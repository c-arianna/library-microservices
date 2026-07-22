package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import java.time.LocalDate;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.LoanStatus;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.LoanUpdatedNotificationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class LoanUpdatedV1NotificationHandler extends AbstractNotificationHandler {
	
	private final ObjectMapper mapper;
	private final SimpMessagingTemplate messagingTemplate;
	
	public LoanUpdatedV1NotificationHandler(ObjectMapper mapper, SimpMessagingTemplate messagingTemplate) {
		this.mapper = mapper;
		this.messagingTemplate = messagingTemplate;
	}
	
	@Override
	public NotificationEventType eventType() {
		return NotificationEventType.LOAN_UPDATED;
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
		 
		 LoanStatus status = LoanStatus.valueOf(requiredField(payload, "status"));
		 LocalDate startDate = LocalDate.parse(requiredField(payload, "startDate"));
		 LocalDate endDate = LocalDate.parse(requiredField(payload, "endDate"));
		 
		 LoanUpdatedNotificationPayload notificationPayload = 
	                new LoanUpdatedNotificationPayload(requiredField(payload, "loanId"), requiredField(payload, "isbn"),
	                		requiredField(payload, "userId"), requiredField(payload, "identityProviderId"), 
	                		 requiredField(payload, "cardNumber"), status, startDate, endDate);
		
		  EventNotification notificationEvent = new EventNotification(event.eventType(), notificationPayload);
		  messagingTemplate.convertAndSend("/topic/loans", notificationEvent);
		  messagingTemplate.convertAndSendToUser(notificationPayload.identityProviderId(), "/queue/loans", notificationEvent);
	}

}
