package mentoring.acomi.loanservice.infrastructure.messaging;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookLoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookReservationRejectedIntegrationPayload;
import mentoring.acomi.loanservice.application.errors.NonRetryableEventException;
import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.application.projection.UserProjection;
import mentoring.acomi.loanservice.application.reactor.LoanReactor;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;
import tools.jackson.databind.ObjectMapper;

@Component
public class LoanIntegrationEventListener {

	private final ObjectMapper mapper;
	private final LoanReactor reactor;
	private final UserProjection userProjection;
	private final LoanProjection loanProjection;

	private final Tracer tracer;

	private final Logger logger = LogManager.getLogger(LoanIntegrationEventListener.class);

	private Map<IntegrationEventTypes, Integer> consumerSupportedVersion = 
			Map.ofEntries(Map.entry(IntegrationEventTypes.BOOK_RESERVED, LoanIntegrationConsumerEventVersions.BOOK_RESERVED), 
					      Map.entry(IntegrationEventTypes.BOOK_RESERVATION_REJECTED, LoanIntegrationConsumerEventVersions.BOOK_RESERVATION_REJECTED), 
					      Map.entry(IntegrationEventTypes.BOOK_BORROWED, LoanIntegrationConsumerEventVersions.BOOK_BORROWED), 
					      Map.entry(IntegrationEventTypes.BOOK_BORROW_REJECTED, LoanIntegrationConsumerEventVersions.BOOK_BORROW_REJECTED), 
					      Map.entry(IntegrationEventTypes.USER_SUBSCRIBED, LoanIntegrationConsumerEventVersions.USER_SUBSCRIBED), 
					      Map.entry(IntegrationEventTypes.USER_UNSUBSCRIBED, LoanIntegrationConsumerEventVersions.USER_UNSUBSCRIBED), 
					      Map.entry(IntegrationEventTypes.USER_SUSPENDED, LoanIntegrationConsumerEventVersions.USER_SUSPENDED), 
					      Map.entry(IntegrationEventTypes.USER_UNSUSPENDED, LoanIntegrationConsumerEventVersions.USER_UNSUSPENDED),
					      Map.entry(IntegrationEventTypes.LOAN_REQUESTED, LoanIntegrationConsumerEventVersions.LOAN_REQUESTED),
					      Map.entry(IntegrationEventTypes.LOAN_CONFIRMED, LoanIntegrationConsumerEventVersions.LOAN_CONFIRMED),
					      Map.entry(IntegrationEventTypes.LOAN_CANCELED, LoanIntegrationConsumerEventVersions.LOAN_CANCELED),
					      Map.entry(IntegrationEventTypes.LOAN_RETURNED, LoanIntegrationConsumerEventVersions.LOAN_RETURNED),
					      Map.entry(IntegrationEventTypes.LOAN_RESERVED, LoanIntegrationConsumerEventVersions.LOAN_RESERVED),
					      Map.entry(IntegrationEventTypes.LOAN_FAILED, LoanIntegrationConsumerEventVersions.LOAN_FAILED)
					      );

	public LoanIntegrationEventListener(ObjectMapper mapper, LoanReactor reactor, UserProjection userProjection, LoanProjection loanProjection, Tracer tracer) {
		this.mapper = mapper;
		this.reactor = reactor;
		this.userProjection = userProjection;
		this.loanProjection = loanProjection;
		this.tracer = tracer;
	}

	@RabbitListener(queues = MessagingTopology.LOAN_QUEUE)
	public void onEvent(IntegrationEventEnvelope<?> eventEnvelope) {

		Span span = tracer.currentSpan();

		logger.info("Received event {} traceId={} spanId={}", eventEnvelope.eventType().eventName,
				span != null ? span.context().traceId() : "null", span != null ? span.context().spanId() : "null");

		try {

			checkEventSchemaVersion(eventEnvelope.eventType(), eventEnvelope.schemaVersion());

			switch (eventEnvelope.eventType()) {
			
			case LOAN_REQUESTED -> {
				LoanRequestedIntegrationPayload payload =  mapper.convertValue(eventEnvelope.payload(), LoanRequestedIntegrationPayload.class);
				loanProjection.loanInsert(payload);
			}
			
			case LOAN_CONFIRMED -> {
				LoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class);
				loanProjection.confirmLoan(payload);
			}
			
			case LOAN_CANCELED -> {
				LoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class);
				loanProjection.cancelLoan(payload);
			}
			
			case LOAN_RETURNED -> {
				LoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class);
				loanProjection.returnLoan(payload);
			}
			
			case LOAN_RESERVED -> {
				LoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class);
				loanProjection.reserveLoan(payload);
			}
			
			case LOAN_FAILED -> {
				LoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class);
				loanProjection.failLoan(payload);
			}
			
			case BOOK_RESERVED -> {
				BookLoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(),
						BookLoanIntegrationPayload.class);
				reactor.handleBookReserved(payload);
			}

			case BOOK_RESERVATION_REJECTED -> {
				BookReservationRejectedIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(),
						BookReservationRejectedIntegrationPayload.class);
				reactor.handleBookReservationRejected(payload);
			}

			case BOOK_BORROWED -> {
				BookLoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(),
						BookLoanIntegrationPayload.class);
				reactor.handleBookBorrowed(payload);
			}

			case BOOK_BORROW_REJECTED -> {
				BookBorrowRejectedIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(),
						BookBorrowRejectedIntegrationPayload.class);
				reactor.handleBookBorrowRejected(payload);
			}
			case USER_SUBSCRIBED -> {
				UserSubscribedIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(),
						UserSubscribedIntegrationPayload.class);
				userProjection.handleSubscribeUser(payload);
			}

			case USER_UNSUBSCRIBED, USER_SUSPENDED, USER_UNSUSPENDED -> {
				UserIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(),
						UserIntegrationPayload.class);
				userProjection.handleUpdateUserStatus(payload);
			}

			default ->
				throw new NonRetryableEventException(String.format("Unexpected value: %s", eventEnvelope.eventType()));
			}

			logger.info("Event processed successfully");

		} catch (NonRetryableEventException e) {
			logger.warn("Dropping incompatible event {} version {}, {}", eventEnvelope.eventType(), eventEnvelope.schemaVersion(), e.getMessage());
			return;
		} catch (Exception e) {
			logger.error("Failed to process event {}", eventEnvelope.eventType(), e);
			throw e;
		}

	}

	private void checkEventSchemaVersion(IntegrationEventTypes eventType, int eventSchemaVersion) {

		int supportedVersion = consumerSupportedVersion.getOrDefault(eventType, -1);

		if (supportedVersion == -1) {
			throw new NonRetryableEventException(String.format("Unknown event type: %s", eventType));
		}

		if (eventSchemaVersion > supportedVersion) {
			throw new NonRetryableEventException(String.format("Unsupported newer version: %d > %d", eventSchemaVersion, supportedVersion));
		}

		if (eventSchemaVersion < supportedVersion) {
			logger.warn("Older version detected: {}", eventSchemaVersion);
			return;
		}

	}

}
