package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.LoanUpdatedNotificationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.AbstractNotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerMetadata;

@NotificationHandlerMetadata(notificationEventType = NotificationEventType.LOAN_UPDATED, supportedVersions = {1})
@Component
public class LoanUpdatedNotificationHandler extends AbstractNotificationHandler<LoanUpdatedNotificationPayload> {
	
	private final SimpMessagingTemplate messagingTemplate;
	
	public LoanUpdatedNotificationHandler(EventPayloadMapper mapper, SimpMessagingTemplate messagingTemplate) {
		super(mapper);
		this.messagingTemplate = messagingTemplate;
	}
		
	@Override
	protected Class<LoanUpdatedNotificationPayload> payloadType() {
		return LoanUpdatedNotificationPayload.class;
	}
	
	@Override
	public void process(LoanUpdatedNotificationPayload payload, NotificationEventEnvelope<?> event) {		
		  EventNotification notificationEvent = new EventNotification(event.eventType(), payload);
		  messagingTemplate.convertAndSend("/topic/loans", notificationEvent);
		  messagingTemplate.convertAndSendToUser(payload.identityProviderId(), "/queue/loans", notificationEvent);
	}

}
