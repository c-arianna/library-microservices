package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.application.sender.SmsSender;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookSubscriptionRequestedPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.publisher.NotificationEventPublisher;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.AbstractNotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerMetadata;

@NotificationHandlerMetadata(notificationEventType = NotificationEventType.BOOK_SUBSCRIPTION_REQUESTED, supportedVersions = {1})
@Component
public class BookSubscriptionRequestedV1Handler extends AbstractNotificationHandler<BookSubscriptionRequestedPayload> {
	
	private NotificationEventPublisher publisher;
	private SmsSender smsSender;

	public BookSubscriptionRequestedV1Handler(EventPayloadMapper mapper, NotificationEventPublisher publisher, SmsSender smsSender) {
		super(mapper);
		this.publisher = publisher;
		this.smsSender = smsSender;
	}

	@Override
	protected Class<BookSubscriptionRequestedPayload> payloadType() {
		return BookSubscriptionRequestedPayload.class;
	}
	
	@Override
	public void process(BookSubscriptionRequestedPayload payload, NotificationEventEnvelope<?> event) {
		 String isbn = payload.isbn();
		 String message = "Il libro richiesto, ISBN: %s, titolo: %s, è disponibile per il prestito".formatted(isbn, payload.title());
		 smsSender.send(payload.phoneNumber(), message);
				 
		 publisher.publishBookSubscriptionNotified(payload.subscriptionId(), isbn);
	}
	
}
