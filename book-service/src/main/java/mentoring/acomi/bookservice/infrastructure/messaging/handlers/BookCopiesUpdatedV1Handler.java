package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.projection.BookProjection;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.BookNotificationService;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@HandlerMetadata(eventType = IntegrationEventTypes.BOOK_COPIES_UPDATED, supportedVersions = {1})
@Component
public class BookCopiesUpdatedV1Handler extends AbstractBookNotificationHandler<BookCopiesUpdatedIntegrationPayload> {
	
	private final BookProjection projection;
		
	public BookCopiesUpdatedV1Handler(BookProjection projection, EventPayloadMapper mapper, BookNotificationService notificationService) {
		super(mapper, notificationService);
		this.projection = projection;
	}
	
	@Override
	protected Class<BookCopiesUpdatedIntegrationPayload> payloadType() {
		return BookCopiesUpdatedIntegrationPayload.class;
	}

	@Override
	protected void updateProjection(BookCopiesUpdatedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.updateCopies(payload, event.occurredAt());
	}
	
	@Override
    protected String isbn(BookCopiesUpdatedIntegrationPayload payload) {
        return payload.isbn();
    }

}
