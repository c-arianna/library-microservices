package mentoring.acomi.assistantservice.infrastructure.messaging.notifications.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.projection.ProjectionOperations;
import mentoring.acomi.assistantservice.application.view.UserView;
import mentoring.acomi.assistantservice.infrastructure.messaging.notifications.payload.UserUpdatedNotificationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.AbstractNotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerMetadata;

@NotificationHandlerMetadata(notificationEventType = NotificationEventType.USER_UPDATED, supportedVersions = {1})
@Component
public class UserUpdatedV1NotificationHandler extends AbstractNotificationHandler<UserUpdatedNotificationPayload> {

	private final ProjectionOperations<UserView> projectionOperations;
	
	public UserUpdatedV1NotificationHandler(ProjectionOperations<UserView> projectionOperations, EventPayloadMapper mapper) {
		super(mapper);
		this.projectionOperations = projectionOperations;
	}
	
	@Override
	protected Class<UserUpdatedNotificationPayload> payloadType() {
		return UserUpdatedNotificationPayload.class;
	}
	
	@Override
	public void process(UserUpdatedNotificationPayload payload, NotificationEventEnvelope<?> event) {
		UserView view = getUser(payload);
		projectionOperations.upsert(view, event.occurredAt());		
	}

	public UserView getUser(UserUpdatedNotificationPayload payload) {		
		return new UserView(payload.userId(), payload.email(), payload.userIdentityProviderId(), payload.status());
	}

}
