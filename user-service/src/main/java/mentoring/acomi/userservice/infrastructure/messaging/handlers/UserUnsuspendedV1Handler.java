package mentoring.acomi.userservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.userservice.application.projection.UserProjection;
import mentoring.acomi.userservice.infrastructure.messaging.notifications.UserNotificationService;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;

@HandlerMetadata(eventType = IntegrationEventTypes.USER_UNSUSPENDED, supportedVersions = {1})
@Component
public class UserUnsuspendedV1Handler extends AbstractUserNotificationHandler<UserIntegrationPayload> {

	private final UserProjection projection;
		
	public UserUnsuspendedV1Handler(UserProjection projection, EventPayloadMapper mapper, UserNotificationService notificationService) {
		super(mapper, notificationService);
		this.projection = projection;
	}

	@Override
	protected Class<UserIntegrationPayload> payloadType() {
		return UserIntegrationPayload.class;
	}

	@Override
	protected void updateProjection(UserIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.unsuspendUser(payload, event.occurredAt());
	}
	
	@Override
    protected String userId(UserIntegrationPayload payload) {
        return payload.userId();
    }
	
}