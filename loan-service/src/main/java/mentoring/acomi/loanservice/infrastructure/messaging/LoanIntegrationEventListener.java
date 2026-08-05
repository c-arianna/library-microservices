package mentoring.acomi.loanservice.infrastructure.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import mentoring.acomi.loanservice.application.errors.NonRetryableEventException;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;

@Component
public class LoanIntegrationEventListener {

	private final LoanEventProcessor eventProcessor;

	private final Tracer tracer;

	private final Logger logger = LogManager.getLogger(LoanIntegrationEventListener.class);

	public LoanIntegrationEventListener(LoanEventProcessor eventProcessor, Tracer tracer) {
		this.eventProcessor = eventProcessor;
		this.tracer = tracer;
	}

	@RabbitListener(queues = MessagingTopology.LOAN_QUEUE)
	public void onEvent(IntegrationEventEnvelope<?> eventEnvelope) {

		Span span = tracer.currentSpan();

		logger.info("Received event {} traceId={} spanId={}", eventEnvelope.eventType().eventName,
				span != null ? span.context().traceId() : "null", span != null ? span.context().spanId() : "null");

		try {

			switch (eventEnvelope.eventType()) {
			
			case LOAN_REQUESTED, LOAN_CONFIRMED, LOAN_CANCELED, LOAN_RETURNED, LOAN_RESERVED, LOAN_FAILED, LOAN_CONFIRM_REQUESTED -> eventProcessor.processProducerEvent(eventEnvelope);			
			
			case BOOK_RESERVED, BOOK_RESERVATION_REJECTED, BOOK_BORROWED, BOOK_BORROW_REJECTED, USER_SUBSCRIBED, USER_UNSUBSCRIBED, USER_SUSPENDED, 
				 USER_UNSUSPENDED, LIBRARY_CARD_ASSIGNED, BOOK_REGISTERED -> eventProcessor.processConsumerEvent(eventEnvelope);

			default ->
				throw new NonRetryableEventException(String.format("Unexpected value: %s", eventEnvelope.eventType()));
			}

		} catch (NonRetryableEventException e) {
			logger.warn("Dropping incompatible event {} version {}, {}", eventEnvelope.eventType(), eventEnvelope.schemaVersion(), e.getMessage());
			return;
		} catch (Exception e) {
			logger.error("Failed to process event {}", eventEnvelope.eventType(), e);
			throw e;
		}

	}

}
