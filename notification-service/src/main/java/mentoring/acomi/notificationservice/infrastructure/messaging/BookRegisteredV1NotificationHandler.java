package mentoring.acomi.notificationservice.infrastructure.messaging;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookRegisteredNotificationPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class BookRegisteredV1NotificationHandler extends AbstractNotificationHandler  {

	private final ObjectMapper mapper;
	private final SimpMessagingTemplate messagingTemplate;
	
	public BookRegisteredV1NotificationHandler(ObjectMapper mapper, SimpMessagingTemplate messagingTemplate) {
		this.mapper = mapper;
		this.messagingTemplate = messagingTemplate;
	}
	
	@Override
	public boolean accepts(IntegrationEventEnvelope<?> event) {
		return event.eventType() == IntegrationEventTypes.BOOK_REGISTERED && event.schemaVersion() == 1;
	}
	
	@Override
	public void handleEvent(IntegrationEventEnvelope<?> event) {
		 		 
		 if (event.payload() == null) {
	            throw new NotificationHandlingException("Payload is null");
	     }
		 
		 JsonNode payload = mapper.valueToTree(event.payload());
		 
		 BookRegisteredNotificationPayload notificationPayload = 
	                new BookRegisteredNotificationPayload(
	                		requiredField(payload, "isbn"),
	                		requiredField(payload, "author"),
	                		requiredField(payload, "title"),
	                		optionalField(payload, "description")
	                );
		
		  messagingTemplate.convertAndSend("/topic/books", new EventNotification(event.eventType(), notificationPayload));
	}
		
	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.BOOK_REGISTERED;
	}

		
}
