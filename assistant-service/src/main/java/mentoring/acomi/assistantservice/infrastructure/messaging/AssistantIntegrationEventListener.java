package mentoring.acomi.assistantservice.infrastructure.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import mentoring.acomi.assistantservice.application.errors.NonRetryableEventException;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;

@Component
public class AssistantIntegrationEventListener {

	private final Tracer tracer;
	private final AssistantEventProcessor eventProcessor;
	
	private final Logger logger = LogManager.getLogger(AssistantIntegrationEventListener.class);
	
	public AssistantIntegrationEventListener(Tracer tracer, AssistantEventProcessor eventProcessor) {
		this.tracer = tracer;
		this.eventProcessor = eventProcessor;
	}

	@RabbitListener(queues = MessagingTopology.ASSISTANT_QUEUE)
	public void onEvent(NotificationEventEnvelope<?> eventEnvelope) {

		Span span = tracer.currentSpan();

		logger.info("Received event {} traceId={} spanId={}", eventEnvelope.eventType().name(),
				span != null ? span.context().traceId() : "null", span != null ? span.context().spanId() : "null");

		try {

			switch (eventEnvelope.eventType()) {

			case  BOOK_UPDATED, LOAN_UPDATED, USER_UPDATED-> {
				eventProcessor.processConsumerEvent(eventEnvelope);
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