package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.projection.BookProjection;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.BOOK_COPIES_UPDATED, supportedVersions = {1})
@Component
public class BookCopiesUpdatedV1Handler extends AbstractEventHandler<BookCopiesUpdatedIntegrationPayload> {
	
	private final BookProjection projection;
		
	public BookCopiesUpdatedV1Handler(BookProjection projection, EventPayloadMapper mapper) {
		super(mapper);
		this.projection = projection;
	}
	
	@Override
	protected Class<BookCopiesUpdatedIntegrationPayload> payloadType() {
		return BookCopiesUpdatedIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(BookCopiesUpdatedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.updateCopies(payload, event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.isbn(), event.schemaVersion()));
	}

}
