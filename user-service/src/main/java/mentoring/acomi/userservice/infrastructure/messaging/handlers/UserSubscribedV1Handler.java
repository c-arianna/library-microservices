package mentoring.acomi.userservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.userservice.application.projection.UserProjection;
import mentoring.acomi.userservice.infrastructure.messaging.notifications.UserNotificationService;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;

@Component
public class UserSubscribedV1Handler extends AbstractUserNotificationHandler<UserSubscribedIntegrationPayload> {

	private final UserProjection projection;
		
	public UserSubscribedV1Handler(UserProjection projection, EventPayloadMapper mapper, UserNotificationService notificationService) {
		super(mapper, notificationService);
		this.projection = projection;
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
	protected void updateProjection(UserSubscribedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.subscribeUser(payload, event.occurredAt());
	}
	
	@Override
    protected String userId(UserSubscribedIntegrationPayload payload) {
        return payload.userId();
    }
	
}
