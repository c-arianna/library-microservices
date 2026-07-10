package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookUpdatedNotificationPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class BookUpdatedV1NotificationHandler extends AbstractNotificationHandler  {

	private final ObjectMapper mapper;
	private final SimpMessagingTemplate messagingTemplate;
	
	public BookUpdatedV1NotificationHandler(ObjectMapper mapper, SimpMessagingTemplate messagingTemplate) {
		this.mapper = mapper;
		this.messagingTemplate = messagingTemplate;
	}
	
	@Override
	public NotificationEventType eventType() {
		return NotificationEventType.BOOK_UPDATED;
	}
	
	@Override
	public boolean accepts(NotificationEventEnvelope<?> event) {
		return event.eventType() == eventType() && event.schemaVersion() == 1;
	}
	
	@Override
	public void handleEvent(NotificationEventEnvelope<?> event) {
		 		 
		 if (event.payload() == null) {
	            throw new NotificationHandlingException("Payload is null");
	     }
		 
		 JsonNode payload = mapper.valueToTree(event.payload());
		 
		 BookUpdatedNotificationPayload notificationPayload = 
	                new BookUpdatedNotificationPayload(
	                		requiredField(payload, "isbn"),
	                		requiredField(payload, "author"),
	                		requiredField(payload, "title"),
	                		optionalField(payload, "description"),
	                		requiredBooleanField(payload, "available"),
	                		requiredIntField(payload, "totalCopies"),
	                		requiredIntField(payload, "borrowedCopies"),
	                		requiredIntField(payload, "reservedCopies")
	                );
		
		  messagingTemplate.convertAndSend("/topic/books", new EventNotification(event.eventType(), notificationPayload));
	}
				
}
