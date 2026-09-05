package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.projection.BookRequestProjectionOperations;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestApprovedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.BOOK_REQUEST_APPROVED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
@Component
public class BookRequestApprovedV1Handler extends AbstractEventHandler<BookRequestApprovedIntegrationPayload> {

	private final BookRequestProjectionOperations projectionOperations;
	
	public BookRequestApprovedV1Handler(@Qualifier("liveBookRequestProjection") BookRequestProjectionOperations projectionOperations, 
			EventPayloadMapper mapper) {
		super(mapper);
		this.projectionOperations = projectionOperations;
	}

	@Override
	protected Class<BookRequestApprovedIntegrationPayload> payloadType() {
		return BookRequestApprovedIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(BookRequestApprovedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projectionOperations.approve(payload.requestId(), event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.requestId()));
	}

}
