package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.UserUpdatedNotificationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.AbstractNotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerMetadata;

@NotificationHandlerMetadata(notificationEventType = NotificationEventType.USER_UPDATED, supportedVersions = { 1 })
@Component
public class UserUpdatedNotificationHandler extends AbstractNotificationHandler<UserUpdatedNotificationPayload> {

	private final SimpMessagingTemplate messagingTemplate;

	public UserUpdatedNotificationHandler(EventPayloadMapper mapper, SimpMessagingTemplate messagingTemplate) {
		super(mapper);
		this.messagingTemplate = messagingTemplate;
	}
	
	@Override
	protected Class<UserUpdatedNotificationPayload> payloadType() {
		return UserUpdatedNotificationPayload.class;
	}

	@Override
	public void process(UserUpdatedNotificationPayload payload, NotificationEventEnvelope<?> event) {
		EventNotification notificationEvent = new EventNotification(event.eventType(), payload);
		messagingTemplate.convertAndSend("/topic/users", notificationEvent);
		messagingTemplate.convertAndSendToUser(payload.userIdentityProviderId(), "/queue/users", notificationEvent);
	}

}
