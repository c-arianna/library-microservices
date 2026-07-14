package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.UserProjection;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class UserSubscribedV1Handler extends AbstractEventHandler<UserSubscribedIntegrationPayload> {

	private final UserProjection userProjection;

	public UserSubscribedV1Handler(UserProjection userProjection, EventPayloadMapper mapper) {
		super(mapper);
		this.userProjection = userProjection;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.USER_SUBSCRIBED;
	}

	@Override
	protected int supportedSchemaVersion() {
		return 1;
	}
	
	@Override
	protected Class<UserSubscribedIntegrationPayload> payloadType() {
		return UserSubscribedIntegrationPayload.class;
	}
	
	@Override
	protected void process(UserSubscribedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		userProjection.handleSubscribeUser(payload, event.occurredAt());
	}

}
