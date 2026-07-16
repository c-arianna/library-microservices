package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.projection.BookProjection;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.BOOK_BORROWED, supportedVersions = {1})
@Component
public class BookBorrowedV1Handler extends AbstractEventHandler<BookLoanIntegrationPayload> {

	private final BookProjection projection;

	public BookBorrowedV1Handler(BookProjection projection, EventPayloadMapper mapper) {
		super(mapper);
		this.projection = projection;
	}

	@Override
	protected Class<BookLoanIntegrationPayload> payloadType() {
		return BookLoanIntegrationPayload.class;
	}
	
	@Override
	protected Optional<ProjectionUpdateNotification> process(BookLoanIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.borrow(payload, event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.isbn(), event.schemaVersion()));
	}

}
