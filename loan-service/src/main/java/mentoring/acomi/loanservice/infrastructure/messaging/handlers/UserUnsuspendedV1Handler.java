package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.UserProjection;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@HandlerMetadata(eventType = IntegrationEventTypes.USER_UNSUSPENDED, supportedVersions = {1})
@Component
public class UserUnsuspendedV1Handler extends AbstractEventHandler<UserIntegrationPayload> {

	private final UserProjection userProjection;
	
	public UserUnsuspendedV1Handler(UserProjection userProjection, EventPayloadMapper mapper) {
		super(mapper);
		this.userProjection = userProjection;
	}
	
	@Override
	protected Class<UserIntegrationPayload> payloadType() {
		return UserIntegrationPayload.class;
	}
	
	@Override
	protected void process(UserIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		userProjection.handleUpdateUserStatus(payload, event.occurredAt());
	}

}
