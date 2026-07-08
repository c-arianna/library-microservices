package mentoring.acomi.loanservice.integration.rabbitmq;

import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import mentoring.acomi.loanservice.infrastructure.messaging.LoanIntegrationConsumerEventVersions;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookLoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookReservationRejectedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.services.LoanService;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.config.RabbitMQConfigTest;
import mentoring.acomi.loanservice.config.SecurityTestConfig;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.domain.events.LoanEventType;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.dto.AddLoanRequest;
import mentoring.acomi.loanservice.infrastructure.dto.LoanResponse;

@SpringBootTest
@Testcontainers
@Import({ RabbitMQConfigTest.class, SecurityTestConfig.class })
class LoanRabbitIntegrationTest {

	@Container
	static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

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
	private AmqpAdmin amqpAdmin;

	@Autowired
	private LoanService loanService;

	@Autowired
	private LoanEventRepository loanEventRepository;

	@Autowired
	private LoanViewQueryRepository loanViewRepository;

	@Autowired
	private TopicExchange eventsExchange;

	@Autowired
	private UserViewRepository userViewRepository;
	
	@Autowired
	private UserViewQueryRepository userViewQueryRepository;
	
	private static final String ISBN = "9788804336327";
	private static final String USER_ID = "user-1";

	private static final String TOKEN_VALUE = "test-token";

	@BeforeEach
	public void setupUser() {
		userViewRepository.add(new UserView(USER_ID, String.format("test%s@gmail.com", USER_ID), UserStatus.ACTIVE), Instant.now());
		setAuthenticatedUser(USER_ID, "READER");
	}
	
	@AfterAll
	static void stopListeners(@Autowired RabbitListenerEndpointRegistry registry) {
	    registry.stop();
	}
	
	@Test
	void shouldConsumeBookReservedAndReserveLoan() {

		String loanId = createLoan();
		publishBookReserved(loanId);

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var loan = loanViewRepository.findById(loanId);
			Assertions.assertTrue(loan.isPresent());
			Assertions.assertEquals(LoanStatus.RESERVED, loan.get().status());
		});

		var events = loanEventRepository.loadStream(loanId);
		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == LoanEventType.LoanReserved));
	}

	@Test
	void shouldConsumeBookReservationRejectedAndFailLoanWhenBookNotAvailable() {

		String loanId = createLoan();
		publishBookReservationRejected(loanId, "BOOK_NOT_AVAILABLE");

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var loan = loanViewRepository.findById(loanId);
			Assertions.assertTrue(loan.isPresent());
			Assertions.assertEquals(LoanStatus.FAILED, loan.get().status());
		});

		var events = loanEventRepository.loadStream(loanId);
		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == LoanEventType.LoanFailed));
	}

	@Test
	void shouldConsumeBookBorrowedAndConfirmLoan() {

		String loanId = createLoan();
		publishBookReserved(loanId);

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var loan = loanViewRepository.findById(loanId);
			Assertions.assertTrue(loan.isPresent());
			Assertions.assertEquals(LoanStatus.RESERVED, loan.get().status());
		});

		publishBookBorrowed(loanId);

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var loan = loanViewRepository.findById(loanId);
			Assertions.assertTrue(loan.isPresent());
			Assertions.assertEquals(LoanStatus.CONFIRMED, loan.get().status());
		});

		var events = loanEventRepository.loadStream(loanId);
		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == LoanEventType.LoanConfirmed));
	}
	
	@Test
	void shouldConsumeBookBorrowRejectedAndFailLoanWhenReservationMissing() {
		String loanId = createLoan();

		publishBookBorrowRejected(loanId, "RESERVATION_MISSING");

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var loan = loanViewRepository.findById(loanId);
			Assertions.assertTrue(loan.isPresent());
			Assertions.assertEquals(LoanStatus.FAILED, loan.get().status());
		});

		var events = loanEventRepository.loadStream(loanId);
		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == LoanEventType.LoanFailed));
	}

	@Test
	void shouldPublishLoanRequestedWhenLoanIsCreated() {

		String queue = createTmpQueue(IntegrationEventTypes.LOAN_REQUESTED.getRoutingKey());
		createLoan();

		String body = waitForMessageBody(queue);

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			Assertions.assertNotNull(body);
			Assertions.assertTrue(body.contains("\"eventType\":\"LOAN_REQUESTED\""));
			Assertions.assertTrue(body.contains("\"producer\":\"loan-service\""));
			Assertions.assertTrue(body.contains(String.format("\"isbn\":\"%s\"", ISBN)));
			Assertions.assertTrue(body.contains(String.format("\"userId\":\"%s\"", USER_ID)));
		});
	}

	@Test
	void shouldPublishLoanReservedAfterConsumingBookReserved() {

		String queue = createTmpQueue(IntegrationEventTypes.LOAN_RESERVED.getRoutingKey());
		String loanId = createLoan();

		publishBookReserved(loanId);

		String body = waitForMessageBody(queue);

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			Assertions.assertNotNull(body);
			Assertions.assertTrue(body.contains("\"eventType\":\"LOAN_RESERVED\""));
			Assertions.assertTrue(body.contains("\"producer\":\"loan-service\""));
			Assertions.assertTrue(body.contains(String.format("\"loanId\":\"%s\"", loanId)));
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

		String loanId = createLoan();

		var event = new IntegrationEventEnvelope<>(String.format("evt-book-reserved-%s", UUID.randomUUID().toString()),
				IntegrationEventTypes.BOOK_RESERVED, "book-service", ISBN, AggregateType.BOOK.name(), 0, Instant.now(),
				LoanIntegrationConsumerEventVersions.BOOK_RESERVED,
				new BookLoanIntegrationPayload(ISBN, loanId, USER_ID));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE,
				IntegrationEventTypes.BOOK_RESERVED.getRoutingKey(), event);

		await().atMost(Duration.ofSeconds(5));

		var events = loanEventRepository.loadStream(loanId);
		Assertions.assertFalse(events.stream().anyMatch(e -> e.type() == LoanEventType.LoanReserved));
	}

	@Test
	void shouldNotConsumeDuplicateEventsTwice() {

		String loanId = createLoan();

		var event = new IntegrationEventEnvelope<>(String.format("evt-book-reserved-%s", UUID.randomUUID().toString()),
				IntegrationEventTypes.BOOK_RESERVED, "book-service", ISBN, AggregateType.BOOK.name(), 0, Instant.now(),
				LoanIntegrationConsumerEventVersions.BOOK_RESERVED,
				new BookLoanIntegrationPayload(ISBN, loanId, USER_ID));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE,
				IntegrationEventTypes.BOOK_RESERVED.getRoutingKey(), event);

		await().atMost(Duration.ofSeconds(5));

		var events = loanEventRepository.loadStream(loanId);
		int eventsSize = events.size();
		Assertions.assertFalse(events.stream().anyMatch(e -> e.type() == LoanEventType.LoanReserved));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, IntegrationEventTypes.BOOK_RESERVED.getRoutingKey(), event);

		var eventsAfterSecondPublish = loanEventRepository.loadStream(loanId);

		await().during(Duration.ofMillis(300)).atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
			Assertions.assertTrue(eventsAfterSecondPublish.size() == eventsSize);
		});
	}

	private String createLoan() {

		LoanResponse response = loanService.addLoan(new AddLoanRequest(ISBN, USER_ID, LocalDate.now(), null));

		Assertions.assertNotNull(response);
		Assertions.assertNotNull(response.loanId());
		return response.loanId();
	}

	private void publishBookReserved(String loanId) {
		var event = new IntegrationEventEnvelope<>(String.format("evt-book-reserved-%s", UUID.randomUUID().toString()),
				IntegrationEventTypes.BOOK_RESERVED, "book-service", ISBN, AggregateType.BOOK.name(), 0, Instant.now(),
				1, new BookLoanIntegrationPayload(ISBN, loanId, USER_ID));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE,
				IntegrationEventTypes.BOOK_RESERVED.getRoutingKey(), event);
	}

	private void publishBookBorrowed(String loanId) {
		var event = new IntegrationEventEnvelope<>(String.format("evt-book-borrowed-%s", UUID.randomUUID().toString()),
				IntegrationEventTypes.BOOK_BORROWED, "book-service", ISBN, AggregateType.BOOK.name(), 1, Instant.now(),
				1, new BookLoanIntegrationPayload(ISBN, loanId, USER_ID));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE,
				IntegrationEventTypes.BOOK_BORROWED.getRoutingKey(), event);
	}

	private void publishBookReservationRejected(String loanId, String reason) {
		var event = new IntegrationEventEnvelope<>(String.format("evt-book-reject-%s", UUID.randomUUID().toString()),
				IntegrationEventTypes.BOOK_RESERVATION_REJECTED, "book-service", ISBN, AggregateType.BOOK.name(), 0,
				Instant.now(), 1, new BookReservationRejectedIntegrationPayload(ISBN, loanId, USER_ID, reason));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE,
				IntegrationEventTypes.BOOK_RESERVATION_REJECTED.getRoutingKey(), event);
	}

	private void publishBookBorrowRejected(String loanId, String reason) {
		var event = new IntegrationEventEnvelope<>(
				String.format("evt-book-borrow-reject-%s", UUID.randomUUID().toString()),
				IntegrationEventTypes.BOOK_BORROW_REJECTED, "book-service", ISBN, AggregateType.BOOK.name(), 0,
				Instant.now(), 1, new BookBorrowRejectedIntegrationPayload(ISBN, loanId, USER_ID, reason));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE,
				IntegrationEventTypes.BOOK_BORROW_REJECTED.getRoutingKey(), event);
	}

	private void publishUserSubscribed(String userId) {
		
		var event = new IntegrationEventEnvelope<>(
				String.format("evt-user-subscribed-%s", UUID.randomUUID().toString()),
				IntegrationEventTypes.USER_SUBSCRIBED, "user-service", userId, AggregateType.USER.name(), 0,
				Instant.now(), 1, new UserSubscribedIntegrationPayload(userId, "test.%s@test.com".formatted(userId), "Test", "Test", "1234", 
						UserStatus.ACTIVE, UserRole.READER));
		
		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE,
				IntegrationEventTypes.USER_SUBSCRIBED.getRoutingKey(), event);
		
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

	private void setAuthenticatedUser(String userId, String role) {

		Jwt jwt = Jwt.withTokenValue(TOKEN_VALUE).header("alg", "none")
				.claim("email", String.format("test%s@gmail.com", userId))
				.claim("realm_access", Map.of("roles", List.of(role))).build();

		Authentication auth = new JwtAuthenticationToken(jwt,
				List.of(new SimpleGrantedAuthority(String.join("_", "ROLE", role))));

		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	private String waitForMessageBody(String queueName) {
		final String[] holder = new String[1];

		await().atMost(Duration.ofSeconds(3)).pollInterval(Duration.ofMillis(100)).until(() -> {
			holder[0] = receiveMessageBody(queueName);
			return holder[0] != null;
		});

		return holder[0];
	}

}