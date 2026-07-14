package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.projection.BookProjection;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.BookNotificationService;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class BookReturnedV1Handler extends AbstractBookNotificationHandler<BookLoanIntegrationPayload> {

	private final BookProjection projection;
	
	public BookReturnedV1Handler(BookProjection projection, EventPayloadMapper mapper, BookNotificationService notificationService) {
		super(mapper, notificationService);
		this.projection = projection;
	}
	
	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.BOOK_RETURNED;
	}

	@Override
	protected int supportedSchemaVersion() {
		return 1;
	}

	@Override
	protected Class<BookLoanIntegrationPayload> payloadType() {
		return BookLoanIntegrationPayload.class;
	}

	@Override
	protected void updateProjection(BookLoanIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.returnBorrowed(payload, event.occurredAt());
	}
	
	@Override
    protected String isbn(BookLoanIntegrationPayload payload) {
        return payload.isbn();
    }

}
