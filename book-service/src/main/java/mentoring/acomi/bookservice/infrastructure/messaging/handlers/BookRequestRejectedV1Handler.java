package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.projection.BookRequestProjectionOperations;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestRejectedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.BOOK_REQUEST_REJECTED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
@Component
public class BookRequestRejectedV1Handler extends AbstractEventHandler<BookRequestRejectedIntegrationPayload>  {

	private final BookRequestProjectionOperations projectionOperations;
	
	public BookRequestRejectedV1Handler(@Qualifier("liveBookRequestProjection") BookRequestProjectionOperations projectionOperations, 
			EventPayloadMapper mapper) {
		super(mapper);
		this.projectionOperations = projectionOperations;
	}

	@Override
	protected Class<BookRequestRejectedIntegrationPayload> payloadType() {
		return BookRequestRejectedIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(BookRequestRejectedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projectionOperations.reject(payload.requestId(), event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.requestId()));
	}
}
