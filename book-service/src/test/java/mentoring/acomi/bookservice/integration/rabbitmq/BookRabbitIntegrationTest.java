package mentoring.acomi.bookservice.integration.rabbitmq;

import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import mentoring.acomi.bookservice.application.outbox.OutboxPublisher;
import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.application.repositories.BookViewQueryRepository;
import mentoring.acomi.bookservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.bookservice.application.services.BookService;
import mentoring.acomi.bookservice.config.RabbitMQConfigTest;
import mentoring.acomi.bookservice.config.SecurityTestConfig;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.domain.events.book.BookEventType;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookCopiesRequest;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookDto;
import mentoring.acomi.bookservice.infrastructure.messaging.BookIntegrationConsumerEventVersions;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanRequestedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@SpringBootTest
@Testcontainers
@Import({ RabbitMQConfigTest.class, SecurityTestConfig.class })
public class BookRabbitIntegrationTest {

	@Container
	private static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

	@DynamicPropertySource
	static void rabbitProps(DynamicPropertyRegistry registry) {
		registry.add("spring.rabbitmq.host", rabbit::getHost);
		registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
		registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
		registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
	}

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private BookService bookService;

	@Autowired
	private BookEventRepository eventRepository;

	@Autowired
	private BookViewQueryRepository viewRepository;

	@Autowired
	private TopicExchange eventsExchange;

	@Autowired
	private AmqpAdmin amqpAdmin;
	
	@Autowired
	private OutboxPublisher outboxPublisher;

	@Autowired
	private UserViewQueryRepository userViewQueryRepository;
	
	private static final String ISBN = "9788804336327";
	private static final String USER_ID = "user-1";
	private static final String LOAN_ID = "loan-1";

	@BeforeEach
	void setupBook() {
		bookService.addBook(new AddBookDto(ISBN, "Italo Calvino", "Il barone rampante", ""));
		bookService.addBookCopies(new AddBookCopiesRequest(3), ISBN);

		await().atMost(Duration.ofSeconds(100)).untilAsserted(() -> {
			var book = viewRepository.findById(ISBN);
			Assertions.assertTrue(book.isPresent());
			Assertions.assertEquals(3, book.get().availableCopies());
		});

	}
	
	@AfterEach
	void stopListeners() {
	    eventRepository.deleteAll();
	}
	
	@AfterAll
	static void stopListeners(@Autowired RabbitListenerEndpointRegistry registry) {
	    registry.stop();
	}
	
	@Test
	void shouldConsumeLoanRequestedAndReserveBook() {

		publishLoanRequestedEvent();
		
		await().untilAsserted(() -> {
			Assertions.assertTrue(
					eventRepository.loadStream(ISBN)
		            .stream()
		            .anyMatch(e -> e.type() == BookEventType.BookReserved)
		    );
		});
		
		outboxPublisher.publishPendingEvents();

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var book = viewRepository.findById(ISBN);
			Assertions.assertTrue(book.isPresent());
			Assertions.assertEquals(1, book.get().reservedCopies());
		});

		var events = eventRepository.loadStream(ISBN);

		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == BookEventType.BookReserved));
	}

	@Test
	void shouldConsumeLoanConfirmRequestedAndBorrowBook() {

		publishLoanRequestedEvent();

		outboxPublisher.publishPendingEvents();
		
		await().untilAsserted(() -> {
			var book = viewRepository.findById(ISBN);
			Assertions.assertTrue(book.isPresent());
			Assertions.assertEquals(1, book.get().reservedCopies());
		});
		
		publishLoanConfirmedRequestEvent();

		outboxPublisher.publishPendingEvents();
		
		await().untilAsserted(() -> {
			var book = viewRepository.findById(ISBN);
			Assertions.assertTrue(book.isPresent());
			Assertions.assertEquals(1, book.get().borrowedCopies());
		});

		var events = eventRepository.loadStream(ISBN);

		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == BookEventType.BookBorrowed));
	}

	@Test
	void shouldRejectInvalidPayload() {

		IntegrationEventEnvelope<Object> invalidEvent = new IntegrationEventEnvelope<>("evt-invalid",
				IntegrationEventTypes.LOAN_REQUESTED, "loan-service", ISBN, AggregateType.LOAN.name(), 0, Instant.now(),
				1, new Object());

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, IntegrationEventTypes.LOAN_REQUESTED.eventName,
				invalidEvent);

		await().atMost(Duration.ofSeconds(3));

		var events = eventRepository.loadStream(ISBN);

		Assertions.assertFalse(events.stream().anyMatch(e -> e.type() == BookEventType.BookReserved));
	}

	@Test
	void shouldPublishBookReservedEvent() {

		String routingKey = IntegrationEventTypes.BOOK_RESERVED.getRoutingKey();
		String tmpQueue = createTmpQueue(routingKey);

		await().atMost(Duration.ofSeconds(1)).until(() -> true);

		publishLoanRequestedEvent();
		
		outboxPublisher.publishPendingEvents();

		await().atMost(Duration.ofSeconds(10000)).untilAsserted(() -> {
			var book = viewRepository.findById(ISBN);
			Assertions.assertTrue(book.isPresent());
			Assertions.assertEquals(1, book.get().reservedCopies());
		});

		String body = waitForMessageBody(tmpQueue);

		await().atMost(Duration.ofSeconds(5)).pollInterval(Duration.ofMillis(100)).untilAsserted(() -> {
			Assertions.assertNotNull(body);
			Assertions.assertTrue(body.contains("BOOK_RESERVED"));
		});

	}
	
	@Test
	void shouldConsumeUserSubscribed() {
		
		String userId = UUID.randomUUID().toString();
		
		publishUserSubscribed(userId);

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			Assertions.assertTrue(userViewQueryRepository.findById(userId).isPresent());
		});
		
	}

	@Test
	void shouldRejectNotSupportedSchemaVersion() {

		IntegrationEventEnvelope<Object> invalidEvent = new IntegrationEventEnvelope<>("evt-invalid",
				IntegrationEventTypes.LOAN_REQUESTED, "loan-service", ISBN, AggregateType.LOAN.name(), 0, Instant.now(),
				BookIntegrationConsumerEventVersions.LOAN_REQUESTED + 1, new Object());

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, IntegrationEventTypes.LOAN_REQUESTED.eventName,
				invalidEvent);

		await().atMost(Duration.ofSeconds(3));

		var events = eventRepository.loadStream(ISBN);

		Assertions.assertFalse(events.stream().anyMatch(e -> e.type() == BookEventType.BookReserved));
	}

	@Test
	void shouldNotConsumeDuplicateEventsTwice() {

		IntegrationEventTypes type = IntegrationEventTypes.LOAN_REQUESTED;
		String eventId = UUID.randomUUID().toString();
		IntegrationEventEnvelope<LoanRequestedIntegrationPayload> event = new IntegrationEventEnvelope<>(eventId, type,
				"loan-service", LOAN_ID, AggregateType.LOAN.name(), 0, Instant.now(), 1,
				new LoanRequestedIntegrationPayload(LOAN_ID, ISBN, USER_ID, LocalDate.now(),
						LocalDate.now().plusDays(30)));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, type.getRoutingKey(), event);


		await().untilAsserted(() -> {
			Assertions.assertTrue(
					eventRepository.loadStream(ISBN)
		            .stream()
		            .anyMatch(e -> e.type() == BookEventType.BookReserved)
		    );
		});
		
		outboxPublisher.publishPendingEvents();
		
		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var book = viewRepository.findById(ISBN);
			Assertions.assertTrue(book.isPresent());
			Assertions.assertEquals(1, book.get().reservedCopies());
		});

		var events = eventRepository.loadStream(ISBN);
		int eventsSize = events.size();

		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == BookEventType.BookReserved));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, type.getRoutingKey(), event);

		var eventsAfterSecondPublish = eventRepository.loadStream(ISBN);

		await().during(Duration.ofMillis(300)).atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
			Assertions.assertTrue(eventsAfterSecondPublish.size() == eventsSize);
		});

	}

	private void publishLoanConfirmedRequestEvent() {

		IntegrationEventTypes type = IntegrationEventTypes.LOAN_CONFIRM_REQUESTED;
		String eventId = UUID.randomUUID().toString();
		IntegrationEventEnvelope<LoanIntegrationPayload> event = new IntegrationEventEnvelope<>(eventId, type,
				"loan-service", LOAN_ID, AggregateType.LOAN.name(), 1, Instant.now(), 1,
				new LoanIntegrationPayload(LOAN_ID, ISBN, USER_ID));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, type.getRoutingKey(), event);
	}

	private void publishLoanRequestedEvent() {

		IntegrationEventTypes type = IntegrationEventTypes.LOAN_REQUESTED;
		String eventId = UUID.randomUUID().toString();
		IntegrationEventEnvelope<LoanRequestedIntegrationPayload> event = new IntegrationEventEnvelope<>(eventId, type,
				"loan-service", LOAN_ID, AggregateType.LOAN.name(), 0, Instant.now(), 1,
				new LoanRequestedIntegrationPayload(LOAN_ID, ISBN, USER_ID, LocalDate.now(),
						LocalDate.now().plusDays(30)));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, type.getRoutingKey(), event);
	}

	private String createTmpQueue(String routingKey) {

		String queueName = String.join(".", "tmp", routingKey, UUID.randomUUID().toString());

		amqpAdmin.declareExchange(eventsExchange);
		Queue queue = new Queue(queueName);
		amqpAdmin.declareQueue(queue);
		amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(eventsExchange).with(routingKey));

		return queueName;
	}

	private String waitForMessageBody(String queueName) {
		final String[] holder = new String[1];

		await().atMost(Duration.ofSeconds(3)).pollInterval(Duration.ofMillis(100)).until(() -> {
			holder[0] = receiveMessageBody(queueName);
			return holder[0] != null;
		});

		return holder[0];
	}

	private String receiveMessageBody(String queueName) {
		var message = rabbitTemplate.receive(queueName, 3000);
		if (message == null) {
			return null;
		}
		return new String(message.getBody(), StandardCharsets.UTF_8);
	}
	
	private void publishUserSubscribed(String userId) {
		
		var event = new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_SUBSCRIBED, 
				"user-service", userId, AggregateType.USER.name(), 1, Instant.now(), 1, 
				new UserSubscribedIntegrationPayload(userId, "test.%s@test.com".formatted(userId), "Test", "Test", "1234", 
						"LIB-000001", UserStatus.ACTIVE, UserRole.READER));
		
		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE,
				IntegrationEventTypes.USER_SUBSCRIBED.getRoutingKey(), event);
		
	}

}
