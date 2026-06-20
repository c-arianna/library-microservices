package mentoring.acomi.userservice.infrastructure.messaging;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.userservice.application.errors.NonRetryableEventException;
import mentoring.acomi.userservice.application.projection.UserProjection;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;
import tools.jackson.databind.ObjectMapper;

@Component
public class UserIntegrationEventListener {

	private Map<IntegrationEventTypes, Integer> consumerSupportedVersion = 
			Map.ofEntries(Map.entry(IntegrationEventTypes.USER_SUBSCRIBED, UserIntegrationConsumerEventVersions.USER_SUBSCRIBED), 
					      Map.entry(IntegrationEventTypes.USER_UNSUBSCRIBED, UserIntegrationConsumerEventVersions.USER_UNSUBSCRIBED), 
					      Map.entry(IntegrationEventTypes.USER_SUSPENDED, UserIntegrationConsumerEventVersions.USER_SUSPENDED), 
					      Map.entry(IntegrationEventTypes.USER_UNSUSPENDED, UserIntegrationConsumerEventVersions.USER_UNSUSPENDED));
	
	private final ObjectMapper mapper;
	private final mentoring.acomi.userservice.application.projection.UserProjection projection;
	
	private final Logger logger = LogManager.getLogger(UserIntegrationEventListener.class);
	
	private final Tracer tracer;
	
	public UserIntegrationEventListener(UserProjection projection, Tracer tracer, ObjectMapper mapper) {
		this.projection = projection;
		this.tracer = tracer;
		this.mapper = mapper;
	}
	@RabbitListener(queues = MessagingTopology.USER_QUEUE)
	public void onEvent(IntegrationEventEnvelope<?> eventEnvelope) {
		
		Span span = tracer.currentSpan();

		logger.info("Received event {} traceId={} spanId={}", eventEnvelope.eventType().eventName,
				span != null ? span.context().traceId() : "null", span != null ? span.context().spanId() : "null");
		
		try {
			
			checkEventSchemaVersion(eventEnvelope.eventType(), eventEnvelope.schemaVersion());

			switch (eventEnvelope.eventType()) {
			
			case USER_SUBSCRIBED -> {
				UserSubscribedIntegrationPayload payload =  mapper.convertValue(eventEnvelope.payload(), UserSubscribedIntegrationPayload.class);
				projection.subscribeUser(payload);
			}
			
			case USER_UNSUBSCRIBED -> {
				UserIntegrationPayload payload =  mapper.convertValue(eventEnvelope.payload(), UserIntegrationPayload.class);
				projection.unsubscribeUser(payload);
			}
			
			case USER_SUSPENDED -> {
				UserIntegrationPayload payload =  mapper.convertValue(eventEnvelope.payload(), UserIntegrationPayload.class);
				projection.suspendUser(payload);
			}
			
			case USER_UNSUSPENDED -> {
				UserIntegrationPayload payload =  mapper.convertValue(eventEnvelope.payload(), UserIntegrationPayload.class);
				projection.unsuspendUser(payload);
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
