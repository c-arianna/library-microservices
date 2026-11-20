package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookRequestUpdatedNotificationPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.AbstractNotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerMetadata;

@NotificationHandlerMetadata(notificationEventType = NotificationEventType.BOOK_REQUEST_UPDATED, supportedVersions = {1})
@Component
public class BookRequestUpdatedV1NotificationHandler extends AbstractNotificationHandler<BookRequestUpdatedNotificationPayload>  {

	private final SimpMessagingTemplate messagingTemplate;
	
	public BookRequestUpdatedV1NotificationHandler(EventPayloadMapper mapper, SimpMessagingTemplate messagingTemplate) {
		super(mapper);
		this.messagingTemplate = messagingTemplate;
	}
	
	@Override
	protected Class<BookRequestUpdatedNotificationPayload> payloadType() {
		return BookRequestUpdatedNotificationPayload.class;
	}
	
	@Override
	public void process(BookRequestUpdatedNotificationPayload payload, NotificationEventEnvelope<?> event) {		
		messagingTemplate.convertAndSend("/topic/bookRequests", new EventNotification(event.eventType(), payload));
	}
				
}
