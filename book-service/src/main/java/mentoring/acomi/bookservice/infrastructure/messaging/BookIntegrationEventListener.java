package mentoring.acomi.bookservice.infrastructure.messaging;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import mentoring.acomi.bookservice.application.errors.NonRetryableEventException;
import mentoring.acomi.bookservice.application.projection.BookProjection;
import mentoring.acomi.bookservice.application.reactor.BookEventReactor;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;
import tools.jackson.databind.ObjectMapper;

@Component
public class BookIntegrationEventListener {

	private final ObjectMapper mapper;
	private final BookEventReactor reactor;
	private final BookProjection projection;
	private final Tracer tracer;

	private final Logger logger = LogManager.getLogger(BookIntegrationEventListener.class);

	private Map<IntegrationEventTypes, Integer> consumerSupportedVersion = 
			Map.ofEntries(Map.entry(IntegrationEventTypes.LOAN_REQUESTED, BookIntegrationConsumerEventVersions.LOAN_REQUESTED),
					      Map.entry(IntegrationEventTypes.LOAN_CONFIRM_REQUESTED, BookIntegrationConsumerEventVersions.LOAN_CONFIRM_REQUESTED), 
						  Map.entry(IntegrationEventTypes.LOAN_CANCELED, BookIntegrationConsumerEventVersions.LOAN_CANCELED), 
						  Map.entry(IntegrationEventTypes.LOAN_RETURNED, BookIntegrationConsumerEventVersions.LOAN_RETURNED),
						  Map.entry(IntegrationEventTypes.BOOK_REGISTERED, BookIntegrationConsumerEventVersions.BOOK_REGISTERED),
						  Map.entry(IntegrationEventTypes.BOOK_COPIES_UPDATED, BookIntegrationConsumerEventVersions.BOOK_COPIES_UPDATED),
						  Map.entry(IntegrationEventTypes.BOOK_RESERVED, BookIntegrationConsumerEventVersions.BOOK_RESERVED),
						  Map.entry(IntegrationEventTypes.BOOK_BORROWED, BookIntegrationConsumerEventVersions.BOOK_BORROWED),
						  Map.entry(IntegrationEventTypes.BOOK_RELEASED, BookIntegrationConsumerEventVersions.BOOK_RELEASED),
						  Map.entry(IntegrationEventTypes.BOOK_RETURNED, BookIntegrationConsumerEventVersions.BOOK_RETURNED),
						  Map.entry(IntegrationEventTypes.BOOK_RESERVATION_REJECTED, BookIntegrationConsumerEventVersions.BOOK_RESERVATION_REJECTED),
						  Map.entry(IntegrationEventTypes.BOOK_BORROW_REJECTED, BookIntegrationConsumerEventVersions.BOOK_BORROW_REJECTED)
				   );

	public BookIntegrationEventListener(ObjectMapper mapper, BookEventReactor reactor, BookProjection projection,
			Tracer tracer) {
		this.mapper = mapper;
		this.reactor = reactor;
		this.projection = projection;
		this.tracer = tracer;
	}

	@RabbitListener(queues = MessagingTopology.BOOK_QUEUE)
	public void onEvent(IntegrationEventEnvelope<?> eventEnvelope) {

		Span span = tracer.currentSpan();

		logger.info("Received event {} traceId={} spanId={}", eventEnvelope.eventType().eventName,
				span != null ? span.context().traceId() : "null", span != null ? span.context().spanId() : "null");

		try {

			checkEventSchemaVersion(eventEnvelope.eventType(), eventEnvelope.schemaVersion());

			switch (eventEnvelope.eventType()) {

				case LOAN_REQUESTED, LOAN_CONFIRM_REQUESTED, LOAN_CANCELED, LOAN_RETURNED -> {
					handleLoanEvent(eventEnvelope.eventType(), mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class));
				}
				
				case BOOK_REGISTERED -> {
					projection.addBook(mapper.convertValue(eventEnvelope.payload(), BookRegisteredIntegrationPayload.class));
				}
				
				case BOOK_COPIES_UPDATED -> {
					projection.updateCopies(mapper.convertValue(eventEnvelope.payload(), BookCopiesUpdatedIntegrationPayload.class));
				}
				
				case BOOK_RESERVED -> {
					projection.reserve(mapper.convertValue(eventEnvelope.payload(), BookLoanIntegrationPayload.class));
				}
				
				case BOOK_BORROWED -> {
					projection.borrow(mapper.convertValue(eventEnvelope.payload(), BookLoanIntegrationPayload.class));
				}
				
				case BOOK_RELEASED -> {
					projection.release(mapper.convertValue(eventEnvelope.payload(), BookLoanIntegrationPayload.class));
				}
				
				case BOOK_RETURNED -> {
					projection.returnBorrowed(mapper.convertValue(eventEnvelope.payload(), BookLoanIntegrationPayload.class));
				}
			
				default -> 
				throw new IllegalArgumentException(String.format("Unknown event type: %s", eventEnvelope.eventType()));
			}
			
			logger.info("Event processed successfully");
		} catch (NonRetryableEventException e) {
			logger.warn("Dropping incompatible event {} version {}, {}", eventEnvelope.eventType(),
					eventEnvelope.schemaVersion(), e.getMessage());
			return;
		} catch (Exception e) {
			logger.error("Failed to process event {}", eventEnvelope.eventType(), e);
			throw e;
		}
	}

	private void handleLoanEvent(IntegrationEventTypes eventType, LoanIntegrationPayload payload) {
	    validatePayload(eventType, payload);
	    reactor.handle(eventType, payload);
	}

	private void checkEventSchemaVersion(IntegrationEventTypes eventType, int eventSchemaVersion) {

		int supportedVersion = consumerSupportedVersion.getOrDefault(eventType, -1);

		if (supportedVersion == -1) {
			throw new NonRetryableEventException(String.format("Unknown event type: %s", eventType));
		}

		if (eventSchemaVersion > supportedVersion) {
			throw new NonRetryableEventException(
					String.format("Unsupported newer version: %d > %d", eventSchemaVersion, supportedVersion));
		}

		if (eventSchemaVersion < supportedVersion) {
			logger.warn("Older version detected: {}", eventSchemaVersion);
			return;
		}

	}

	private void validatePayload(IntegrationEventTypes eventType, LoanIntegrationPayload payload) {

		if (payload == null || payload.loanId() == null || payload.userId() == null || payload.isbn() == null) {
			throw new IllegalArgumentException(String.format("Invalid payload for event %s", eventType));
		}
	}

}