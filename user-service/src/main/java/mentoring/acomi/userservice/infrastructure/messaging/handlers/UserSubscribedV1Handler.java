package mentoring.acomi.userservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.userservice.application.projection.UserProjection;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;

@Component
public class UserSubscribedV1Handler implements EventHandler {

	private final UserProjection projection;
	private final EventPayloadMapper mapper;
	
	public UserSubscribedV1Handler(UserProjection projection, EventPayloadMapper mapper) {
		this.projection = projection;
		this.mapper = mapper;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.USER_SUBSCRIBED;
	}

	@Override
	public boolean accepts(IntegrationEventEnvelope<?> event) {
		return event.eventType() == eventType() && event.schemaVersion() == 1;
	}

	@Override
	public void handleEvent(IntegrationEventEnvelope<?> event) {
		UserSubscribedIntegrationPayload payload = mapper.mapAndValidate(event.payload(), UserSubscribedIntegrationPayload.class);
		projection.subscribeUser(payload, event.occurredAt());
	}
	
}
