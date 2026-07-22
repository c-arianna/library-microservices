package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.UserUpdatedNotificationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@NotificationHandlerMetadata(notificationEventType = NotificationEventType.USER_UPDATED, supportedVersions = {1,2})
@Component
public class UserUpdatedNotificationHandler extends AbstractNotificationHandler {
	
	private final ObjectMapper mapper;
	private final SimpMessagingTemplate messagingTemplate;
	
	public UserUpdatedNotificationHandler(ObjectMapper mapper, SimpMessagingTemplate messagingTemplate) {
		this.mapper = mapper;
		this.messagingTemplate = messagingTemplate;
	}
	
	@Override
	public void handleEvent(NotificationEventEnvelope<?> event) {
		 		 
		 if (event.payload() == null) {
	            throw new NotificationHandlingException("Payload is null");
	     }
		 
		 JsonNode payload = mapper.valueToTree(event.payload());
		 
		 UserStatus status = UserStatus.valueOf(requiredField(payload, "status"));
		 UserRole role = UserRole.valueOf(requiredField(payload, "role"));
		 
		 UserUpdatedNotificationPayload notificationPayload = 
	                new UserUpdatedNotificationPayload(requiredField(payload, "userId"), requiredField(payload, "email"),
	                		requiredField(payload, "name"),  requiredField(payload, "lastname"), requiredField(payload, "userIdentityProviderId"), 
	                		requiredField(payload, "cardNumber"), status, role);
		
		  EventNotification notificationEvent = new EventNotification(event.eventType(), notificationPayload);
		  messagingTemplate.convertAndSend("/topic/users", notificationEvent);
		  messagingTemplate.convertAndSendToUser(notificationPayload.userIdentityProviderId(), "/queue/users", notificationEvent);
	}



}
