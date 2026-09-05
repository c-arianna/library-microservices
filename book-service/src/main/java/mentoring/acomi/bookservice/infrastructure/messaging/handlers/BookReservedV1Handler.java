package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.projection.BookProjectionOperations;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.BOOK_RESERVED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
@Component
public class BookReservedV1Handler extends AbstractEventHandler<BookLoanIntegrationPayload> {
	
	private final BookProjectionOperations projectionOperations;

	public BookReservedV1Handler(@Qualifier("liveBookProjection") BookProjectionOperations projectionOperations, EventPayloadMapper mapper) {
		super(mapper);
		this.projectionOperations = projectionOperations;
	}

	@Override
	protected Class<BookLoanIntegrationPayload> payloadType() {
		return BookLoanIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(BookLoanIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projectionOperations.reserve(payload.isbn(), event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.isbn()));
	}

}
