package mentoring.acomi.bookservice.infrastructure.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import mentoring.acomi.bookservice.application.errors.NonRetryableEventException;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import tools.jackson.databind.ObjectMapper;

@Component
public class BookIntegrationEventListener {

	private final ObjectMapper mapper;
	private final Tracer tracer;
	private final BookEventProcessor eventProcessor;
	
	private final Logger logger = LogManager.getLogger(BookIntegrationEventListener.class);
	
	public BookIntegrationEventListener(ObjectMapper mapper, Tracer tracer, BookEventProcessor eventProcessor) {
		this.mapper = mapper;
		this.tracer = tracer;
		this.eventProcessor = eventProcessor;
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
				eventProcessor.processConsumerEvent(eventEnvelope,
						mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class));
			}

			case BOOK_REGISTERED , BOOK_COPIES_UPDATED, BOOK_RESERVED, BOOK_BORROWED, BOOK_RELEASED, BOOK_RETURNED, BOOK_BORROW_REJECTED,
			    BOOK_RESERVATION_REJECTED -> {
				eventProcessor.processProducerEvent(eventEnvelope);
			}

			default ->
				throw new NonRetryableEventException(String.format("Unknown event type: %s", eventEnvelope.eventType()));
			}

		} catch (NonRetryableEventException e) {
			logger.warn("Dropping incompatible event {} version {}, {}", eventEnvelope.eventType(),
					eventEnvelope.schemaVersion(), e.getMessage());
			return;
		} catch (Exception e) {
			logger.error("Failed to process event {}, {}", eventEnvelope.eventType(), e);
			throw e;
		}
	}
	
	private void checkEventSchemaVersion(IntegrationEventTypes eventType, int eventSchemaVersion) {

		int supportedVersion = BookEventProcessor.consumerSupportedVersion.getOrDefault(eventType, -1);

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

}