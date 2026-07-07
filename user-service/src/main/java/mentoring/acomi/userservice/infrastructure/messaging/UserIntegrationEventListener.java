package mentoring.acomi.userservice.infrastructure.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.userservice.application.errors.NonRetryableEventException;

@Component
public class UserIntegrationEventListener {
	
	private final UserEventProcessor eventProcessor;
	
	private final Logger logger = LogManager.getLogger(UserIntegrationEventListener.class);
	
	private final Tracer tracer;
	
	public UserIntegrationEventListener(UserEventProcessor eventProcessor, Tracer tracer) {
		this.eventProcessor = eventProcessor;
		this.tracer = tracer;
	}
	
	@RabbitListener(queues = MessagingTopology.USER_QUEUE)
	public void onEvent(IntegrationEventEnvelope<?> eventEnvelope) {
		
		Span span = tracer.currentSpan();

		logger.info("Received event {} traceId={} spanId={}", eventEnvelope.eventType().eventName,
				span != null ? span.context().traceId() : "null", span != null ? span.context().spanId() : "null");
		
		try {
			
			switch (eventEnvelope.eventType()) {
			
			case USER_SUBSCRIBED, USER_UNSUBSCRIBED, USER_SUSPENDED, USER_UNSUSPENDED -> {
				eventProcessor.processProducerEvent(eventEnvelope);
			}
			
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
