package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.projection.BookProjection;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.BookNotificationService;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@HandlerMetadata(eventType = IntegrationEventTypes.BOOK_RESERVED, supportedVersions = {1})
@Component
public class BookReservedV1Handler extends AbstractBookNotificationHandler<BookLoanIntegrationPayload> {
	
	private final BookProjection projection;

	public BookReservedV1Handler(BookProjection projection, EventPayloadMapper mapper, BookNotificationService notificationService) {
		super(mapper, notificationService);
		this.projection = projection;
	}

	@Override
	protected Class<BookLoanIntegrationPayload> payloadType() {
		return BookLoanIntegrationPayload.class;
	}

	@Override
	protected void updateProjection(BookLoanIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.reserve(payload, event.occurredAt());
	}
	
	@Override
    protected String isbn(BookLoanIntegrationPayload payload) {
        return payload.isbn();
    }

}
