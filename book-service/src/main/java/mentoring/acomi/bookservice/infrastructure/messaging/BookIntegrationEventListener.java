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

			switch (eventEnvelope.eventType()) {

			case LOAN_REQUESTED, LOAN_CONFIRM_REQUESTED, LOAN_CANCELED, LOAN_RETURNED,
			USER_SUBSCRIBED, USER_UNSUBSCRIBED -> {
				eventProcessor.processConsumerEvent(eventEnvelope,
						mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class));
			}

			case BOOK_REGISTERED , BOOK_COPIES_UPDATED, BOOK_RESERVED, BOOK_BORROWED, BOOK_RELEASED, BOOK_RETURNED, BOOK_BORROW_REJECTED,
			    BOOK_RESERVATION_REJECTED -> {
				eventProcessor.processProducerBookEvent(eventEnvelope);
			}

			case BOOK_REQUEST_ADDED, BOOK_REQUEST_APPROVED, BOOK_REQUEST_REJECTED, BOOK_REQUEST_VOTED,
			     BOOK_REQUEST_PRICE_UPDATED -> {
				eventProcessor.processProducerBookRquestEvent(eventEnvelope);
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

}