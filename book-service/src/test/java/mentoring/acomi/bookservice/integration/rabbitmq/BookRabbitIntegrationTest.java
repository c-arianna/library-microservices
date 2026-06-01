package mentoring.acomi.bookservice.integration.rabbitmq;

import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.application.repositories.BookViewRepository;
import mentoring.acomi.bookservice.application.services.BookService;
import mentoring.acomi.bookservice.domain.events.BookEventType;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookCopiesRequest;
import mentoring.acomi.bookservice.infrastructure.dto.AddBookRequest;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedlibrary.integration.messaging.loan.LoanIntegrationPayload;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookRabbitIntegrationTest {

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
	private BookViewRepository viewRepository;

	@Autowired
	private TopicExchange eventsExchange;

	@Autowired
	private AmqpAdmin amqpAdmin;

	private static final String ISBN = "9788804336327";
	private static final String USER_ID = "user-1";
	private static final String LOAN_ID = "loan-1";

	@BeforeEach
	void setupBook() {
		bookService.addBook(new AddBookRequest(ISBN, "Italo Calvino", "Il barone rampante", ""));
		bookService.addBookCopies(new AddBookCopiesRequest(3), ISBN);
	}

	@Test
	void shouldConsumeLoanRequestedAndReserveBook() {
		
		publishLoanEvent(IntegrationEventTypes.LOAN_REQUESTED);

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var book = viewRepository.findById(ISBN).orElseThrow();
			Assertions.assertEquals(1, book.reservedCopies());
		});

		var events = eventRepository.loadStream(ISBN);

		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == BookEventType.BookReserved));
	}

	@Test
	void shouldConsumeLoanConfirmRequestedAndBorrowBook() {

		publishLoanEvent(IntegrationEventTypes.LOAN_REQUESTED);
		
		publishLoanEvent(IntegrationEventTypes.LOAN_CONFIRM_REQUESTED);

		await().untilAsserted(() -> {
			var book = viewRepository.findById(ISBN).orElseThrow();
			Assertions.assertEquals(1, book.borrowedCopies());
		});

		var events = eventRepository.loadStream(ISBN);

		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == BookEventType.BookBorrowed));
	}

	@Test
	void shouldRejectInvalidPayload() {

		IntegrationEventEnvelope<Object> invalidEvent = new IntegrationEventEnvelope<>("evt-invalid",
				IntegrationEventTypes.LOAN_REQUESTED, "loan-service", ISBN, Instant.now(), new Object());

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, "loan.requested", invalidEvent);

		await().atMost(Duration.ofSeconds(3));

		var events = eventRepository.loadStream(ISBN);

		Assertions.assertFalse(events.stream().anyMatch(e -> e.type() == BookEventType.BookReserved));
	}

	@Test
	void shouldPublishBookReservedEvent() {

		String probeQueue = createTmpQueue(IntegrationEventTypes.BOOK_RESERVED.toString());

		publishLoanEvent(IntegrationEventTypes.LOAN_REQUESTED);

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			String body = receiveMessageBody(probeQueue);
			Assertions.assertNotNull(body);
			Assertions.assertTrue(body.contains("book.reserved"));
		});
	}

	private void publishLoanEvent(IntegrationEventTypes type) {

		String eventId = UUID.randomUUID().toString();
		IntegrationEventEnvelope<LoanIntegrationPayload> event = new IntegrationEventEnvelope<>(eventId, type,
				"loan-service", ISBN, Instant.now(), new LoanIntegrationPayload(LOAN_ID, ISBN, USER_ID));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, type.toString(), event);
	}

	private String createTmpQueue(String routingKey) {

		String queueName = String.join(".", "tmp", routingKey, UUID.randomUUID().toString());

		Queue queue = new Queue(queueName, false, false, true);
		amqpAdmin.declareQueue(queue);
		amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(eventsExchange).with(routingKey));

		return queueName;
	}

	private String receiveMessageBody(String queueName) {
		var message = rabbitTemplate.receive(queueName, 3000);
		if (message == null) {
			return null;
		}
		return new String(message.getBody(), StandardCharsets.UTF_8);
	}
}
