package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.projection.BookProjectionOperations;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.BOOK_COPIES_UPDATED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
@Component
public class BookCopiesUpdatedV1Handler extends AbstractEventHandler<BookCopiesUpdatedIntegrationPayload> {
	
	private final BookProjectionOperations projectionOperations;
		
	public BookCopiesUpdatedV1Handler(@Qualifier("liveBookProjection") BookProjectionOperations projectionOperations, EventPayloadMapper mapper) {
		super(mapper);
		this.projectionOperations = projectionOperations;
	}
	
	@Override
	protected Class<BookCopiesUpdatedIntegrationPayload> payloadType() {
		return BookCopiesUpdatedIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(BookCopiesUpdatedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projectionOperations.updateCopies(payload.isbn(), payload.quantity(), event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.isbn()));
	}

}
