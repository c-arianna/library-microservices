package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookRequestStatus;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookRequestUpdatedNotificationPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.AbstractNotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.error.NotificationHandlingException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@NotificationHandlerMetadata(notificationEventType = NotificationEventType.BOOK_REQUEST_UPDATED, supportedVersions = {1})
@Component
public class BookRequestUpdatedV1NotificationHandler extends AbstractNotificationHandler  {

	private final ObjectMapper mapper;
	private final SimpMessagingTemplate messagingTemplate;
	
	public BookRequestUpdatedV1NotificationHandler(ObjectMapper mapper, SimpMessagingTemplate messagingTemplate) {
		this.mapper = mapper;
		this.messagingTemplate = messagingTemplate;
	}
	
	@Override
	public void handleEvent(NotificationEventEnvelope<?> event) {
		 		 
		 if (event.payload() == null) {
	            throw new NotificationHandlingException("Payload is null");
	     }
		 
		 JsonNode payload = mapper.valueToTree(event.payload());
		 BookRequestStatus status = BookRequestStatus.valueOf(requiredField(payload, "status"));
		 
		 BookRequestUpdatedNotificationPayload notificationPayload = 
	                new BookRequestUpdatedNotificationPayload(
	                		requiredField(payload, "requestId"),
	                		requiredField(payload, "author"),
	                		requiredField(payload, "title"),
	                		optionalField(payload, "isbn"),
	                		requiredIntField(payload, "votes"),
	                		status);
		
		  messagingTemplate.convertAndSend("/topic/bookRequests", new EventNotification(event.eventType(), notificationPayload));
	}
				
}
