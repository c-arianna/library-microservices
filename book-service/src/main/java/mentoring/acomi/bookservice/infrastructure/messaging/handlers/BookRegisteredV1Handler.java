package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.projection.BookProjection;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.BookNotificationService;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class BookRegisteredV1Handler extends AbstractBookNotificationHandler<BookRegisteredIntegrationPayload> {

	private final BookProjection projection;

	public BookRegisteredV1Handler(BookProjection projection, EventPayloadMapper mapper, BookNotificationService notificationService) {
		super(mapper, notificationService);
		this.projection = projection;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.BOOK_REGISTERED;
	}

	@Override
	protected int supportedSchemaVersion() {
		return 1;
	}

	@Override
	protected Class<BookRegisteredIntegrationPayload> payloadType() {
		return BookRegisteredIntegrationPayload.class;
	}

	@Override
	protected void updateProjection(BookRegisteredIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.addBook(payload, event.occurredAt());
	}
	
	@Override
    protected String isbn(BookRegisteredIntegrationPayload payload) {
        return payload.isbn();
    }

}
