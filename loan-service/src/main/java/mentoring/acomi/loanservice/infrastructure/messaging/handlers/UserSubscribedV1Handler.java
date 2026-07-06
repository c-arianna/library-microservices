package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.UserProjection;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class UserSubscribedV1Handler implements EventHandler {

	private final UserProjection userProjection;
	private final EventPayloadMapper mapper;

	public UserSubscribedV1Handler(UserProjection userProjection, EventPayloadMapper mapper) {
		this.userProjection = userProjection;
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
		userProjection.handleSubscribeUser(payload, event.occurredAt());
	}

}
