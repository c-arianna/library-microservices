package mentoring.acomi.bookservice.infrastructure.messaging.notifications.handlers;

import java.time.Instant;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.projection.BookSubscriptionProjection;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.payload.BookSubscriptionNotifiedPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.AbstractNotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerMetadata;

@NotificationHandlerMetadata(notificationEventType = NotificationEventType.BOOK_SUBSCRIPTION_NOTIFIED, supportedVersions = {1})
@Component
public class BookSubscriptionNotifiedV1Handler extends AbstractNotificationHandler<BookSubscriptionNotifiedPayload> {

	private final BookSubscriptionProjection projection;
	
	public BookSubscriptionNotifiedV1Handler(BookSubscriptionProjection projection, EventPayloadMapper mapper) {
		super(mapper);
		this.projection = projection;
	}

	@Override
	protected Class<BookSubscriptionNotifiedPayload> payloadType() {
		return BookSubscriptionNotifiedPayload.class;
	}
	
	@Override
	public void process(BookSubscriptionNotifiedPayload payload, NotificationEventEnvelope<?> event) {	 
		projection.markAsNotified(payload.subscriptionId(), Instant.now());
	}
}
