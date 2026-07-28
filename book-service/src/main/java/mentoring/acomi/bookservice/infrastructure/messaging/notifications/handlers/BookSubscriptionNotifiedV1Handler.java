package mentoring.acomi.bookservice.infrastructure.messaging.notifications.handlers;

import java.time.Instant;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.projection.BookSubscriptionProjection;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.payload.BookSubscriptionNotifiedPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.AbstractNotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.error.NotificationHandlingException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@NotificationHandlerMetadata(notificationEventType = NotificationEventType.BOOK_SUBSCRIPTION_NOTIFIED, supportedVersions = {1})
@Component
public class BookSubscriptionNotifiedV1Handler extends AbstractNotificationHandler {

	private final ObjectMapper mapper;
	private final BookSubscriptionProjection projection;
	
	public BookSubscriptionNotifiedV1Handler(BookSubscriptionProjection projection, ObjectMapper mapper) {
		this.mapper = mapper;
		this.projection = projection;
	}

	@Override
	public void handleEvent(NotificationEventEnvelope<?> event) {
				
		 if (event.payload() == null) {
	            throw new NotificationHandlingException("Payload is null");
	     }
		 
		 JsonNode payload = mapper.valueToTree(event.payload());
		 
		 BookSubscriptionNotifiedPayload notificationPayload = 
	                new BookSubscriptionNotifiedPayload(
	                		requiredLongField(payload, "subscriptionId"),
	                		requiredField(payload, "isbn"));
		 
		projection.markAsNotified(notificationPayload.subscriptionId(), Instant.now());
	}
}
