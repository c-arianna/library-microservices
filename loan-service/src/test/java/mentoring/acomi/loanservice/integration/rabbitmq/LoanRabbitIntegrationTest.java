package mentoring.acomi.loanservice.integration.rabbitmq;

import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.services.LoanService;
import mentoring.acomi.loanservice.domain.events.LoanEventType;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.dto.AddLoanRequest;
import mentoring.acomi.loanservice.infrastructure.dto.LoanResponse;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedlibrary.integration.messaging.book.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.sharedlibrary.integration.messaging.book.BookLoanIntegrationPayload;
import mentoring.acomi.sharedlibrary.integration.messaging.book.BookReservationRejectedIntegrationPayload;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
	private LoanViewRepository loanViewRepository;

	@Autowired
	private TopicExchange eventsExchange;

	private static final String ISBN = "9788804336327";
	private static final String USER_ID = "user-1";

	@Test
	void shouldConsumeBookReservedAndReserveLoan() {

		String loanId = createLoan();
		publishBookReserved(loanId);

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var loan = loanViewRepository.findById(loanId).orElseThrow();
			Assertions.assertEquals(LoanStatus.RESERVED, loan.status());
		});

		var events = loanEventRepository.loadStream(loanId);
		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == LoanEventType.LoanReserved));
	}

	@Test
	void shouldConsumeBookReservationRejectedAndFailLoanWhenBookNotAvailable() {

		String loanId = createLoan();
		publishBookReservationRejected(loanId, "BOOK_NOT_AVAILABLE");

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var loan = loanViewRepository.findById(loanId).orElseThrow();
			Assertions.assertEquals(LoanStatus.FAILED, loan.status());
		});

		var events = loanEventRepository.loadStream(loanId);
		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == LoanEventType.LoanFailed));
	}

	@Test
	void shouldConsumeBookBorrowedAndConfirmLoan() {

		String loanId = createLoan();
		publishBookReserved(loanId);

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var loan = loanViewRepository.findById(loanId).orElseThrow();
			Assertions.assertEquals(LoanStatus.RESERVED, loan.status());
		});

		publishBookBorrowed(loanId);

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var loan = loanViewRepository.findById(loanId).orElseThrow();
			Assertions.assertEquals(LoanStatus.CONFIRMED, loan.status());
		});

		var events = loanEventRepository.loadStream(loanId);
		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == LoanEventType.LoanConfirmed));
	}

	@Test
	void shouldConsumeBookBorrowRejectedAndFailLoanWhenReservationMissing() {
		String loanId = createLoan();

		publishBookBorrowRejected(loanId, "RESERVATION_MISSING");

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var loan = loanViewRepository.findById(loanId).orElseThrow();
			Assertions.assertEquals(LoanStatus.FAILED, loan.status());
		});

		var events = loanEventRepository.loadStream(loanId);
		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == LoanEventType.LoanFailed));
	}

	@Test
	void shouldPublishLoanRequestedWhenLoanIsCreated() {

		String queue = createTmpQueue(IntegrationEventTypes.LOAN_REQUESTED.toString());
		createLoan();

		String messageBody = receiveMessageBody(queue);

		Assertions.assertNotNull(messageBody);
		Assertions.assertTrue(messageBody.contains("\"eventType\":\"loan.requested\""));
		Assertions.assertTrue(messageBody.contains("\"producer\":\"loan-service\""));
		Assertions.assertTrue(messageBody.contains(String.format("\"isbn\":\"%s\"", ISBN)));
		Assertions.assertTrue(messageBody.contains(String.format("\"userId\":\"%s\"", USER_ID)));
	}

	@Test
	void shouldPublishLoanReservedAfterConsumingBookReserved() {

		String queue = createTmpQueue(IntegrationEventTypes.LOAN_RESERVED.toString());
		String loanId = createLoan();

		publishBookReserved(loanId);

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			String body = receiveMessageBody(queue);
			Assertions.assertNotNull(body);
			Assertions.assertTrue(body.contains("\"eventType\":\"loan.reserved\""));
			Assertions.assertTrue(body.contains("\"producer\":\"loan-service\""));
			Assertions.assertTrue(body.contains(String.format("\"loanId\":\"%s\"", loanId)));
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
				IntegrationEventTypes.BOOK_RESERVED, "book-service", ISBN, Instant.now(),
				new BookLoanIntegrationPayload(ISBN, loanId, USER_ID));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, IntegrationEventTypes.BOOK_RESERVED.toString(),
				event);
	}

	private void publishBookBorrowed(String loanId) {
		var event = new IntegrationEventEnvelope<>(String.format("evt-book-borrowed-%s", UUID.randomUUID().toString()),
				IntegrationEventTypes.BOOK_BORROWED, "book-service", ISBN, Instant.now(),
				new BookLoanIntegrationPayload(ISBN, loanId, USER_ID));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, IntegrationEventTypes.BOOK_BORROWED.toString(),
				event);
	}

	private void publishBookReservationRejected(String loanId, String reason) {
		var event = new IntegrationEventEnvelope<>(String.format("evt-book-reject-%s", UUID.randomUUID().toString()),
				IntegrationEventTypes.BOOK_RESERVATION_REJECTED, "book-service", ISBN, Instant.now(),
				new BookReservationRejectedIntegrationPayload(ISBN, loanId, USER_ID, reason));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE,
				IntegrationEventTypes.BOOK_RESERVATION_REJECTED.toString(), event);
	}

	private void publishBookBorrowRejected(String loanId, String reason) {
		var event = new IntegrationEventEnvelope<>(
				String.format("evt-book-borrow-reject-%s", UUID.randomUUID().toString()),
				IntegrationEventTypes.BOOK_BORROW_REJECTED, "book-service", ISBN, Instant.now(),
				new BookBorrowRejectedIntegrationPayload(ISBN, loanId, USER_ID, reason));

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE,
				IntegrationEventTypes.BOOK_BORROW_REJECTED.toString(), event);
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