package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.application.sender.SmsSender;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookSubscriptionRequestedPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.publisher.NotificationEventPublisher;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.AbstractNotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.error.NotificationHandlingException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@NotificationHandlerMetadata(notificationEventType = NotificationEventType.BOOK_SUBSCRIPTION_REQUESTED, supportedVersions = {1})
@Component
public class BookSubscriptionRequestedV1Handler extends AbstractNotificationHandler {
	
	private final ObjectMapper mapper;
	private NotificationEventPublisher publisher;
	private SmsSender smsSender;

	public BookSubscriptionRequestedV1Handler(ObjectMapper mapper, NotificationEventPublisher publisher, SmsSender smsSender) {
		this.mapper = mapper;
		this.publisher = publisher;
		this.smsSender = smsSender;
	}

	@Override
	public void handleEvent(NotificationEventEnvelope<?> event) {
				
		 if (event.payload() == null) {
	            throw new NotificationHandlingException("Payload is null");
	     }
		 
		 JsonNode payload = mapper.valueToTree(event.payload());
		 
		 BookSubscriptionRequestedPayload notificationPayload = 
	                new BookSubscriptionRequestedPayload(
	                		requiredLongField(payload, "subscriptionId"),
	                		requiredField(payload, "isbn"),
	                		requiredField(payload, "userIdentityId"),
	                		optionalField(payload, "phoneNumber"),
	                		optionalField(payload, "title")
	                );
		 
		 String isbn = notificationPayload.isbn();
		 String message = "Il libro richiesto, ISBN: %s, titolo: %s, è disponibile per il prestito".formatted(isbn, notificationPayload.title());
		 smsSender.send(notificationPayload.phoneNumber(), message);
				 
		 publisher.publishBookSubscriptionNotified(notificationPayload.subscriptionId(), isbn);
	}
	
}
