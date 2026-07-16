package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.UserProjection;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.USER_SUSPENDED, supportedVersions = {1})
@Component
public class UserSuspendedV1Handler extends AbstractEventHandler<UserIntegrationPayload> {
	
	private final UserProjection userProjection;

	public UserSuspendedV1Handler(UserProjection userProjection, EventPayloadMapper mapper) {
		super(mapper);
		this.userProjection = userProjection;
	}
	
	@Override
	protected Class<UserIntegrationPayload> payloadType() {
		return UserIntegrationPayload.class;
	}
	
	@Override
	protected Optional<ProjectionUpdateNotification> process(UserIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		userProjection.handleUpdateUserStatus(payload, event.occurredAt());
		return Optional.empty();
	}

}
