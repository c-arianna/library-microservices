package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookUpdatedNotificationPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.AbstractNotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerMetadata;

@NotificationHandlerMetadata(notificationEventType = NotificationEventType.BOOK_UPDATED, supportedVersions = {1})
@Component
public class BookUpdatedV1NotificationHandler extends AbstractNotificationHandler<BookUpdatedNotificationPayload>  {

	private final SimpMessagingTemplate messagingTemplate;
	
	public BookUpdatedV1NotificationHandler(EventPayloadMapper mapper, SimpMessagingTemplate messagingTemplate) {
		super(mapper);
		this.messagingTemplate = messagingTemplate;
	}
	
	@Override
	protected Class<BookUpdatedNotificationPayload> payloadType() {
		return BookUpdatedNotificationPayload.class;
	}
	
	@Override
	public void process(BookUpdatedNotificationPayload payload, NotificationEventEnvelope<?> event) {		
		messagingTemplate.convertAndSend("/topic/books", new EventNotification(event.eventType(), payload));
	}
				
}
