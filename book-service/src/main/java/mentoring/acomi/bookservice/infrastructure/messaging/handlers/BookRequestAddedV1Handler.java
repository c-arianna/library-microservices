package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.projection.BookRequestProjectionOperations;
import mentoring.acomi.bookservice.application.projection.BookRequestVoteProjectionOperations;
import mentoring.acomi.bookservice.application.view.BookRequestView;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestAddedIntegrationPayload;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.BOOK_REQUEST_ADDED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
@Component
public class BookRequestAddedV1Handler extends AbstractEventHandler<BookRequestAddedIntegrationPayload> {

	private final BookRequestProjectionOperations projectionOperations;
	private final BookRequestVoteProjectionOperations voteProjectionOperations;
	
	public BookRequestAddedV1Handler(@Qualifier("liveBookRequestProjection") BookRequestProjectionOperations projectionOperations, 
			@Qualifier("liveBookRequestVoteProjection") BookRequestVoteProjectionOperations voteProjectionOperations, EventPayloadMapper mapper) {
		super(mapper);
		this.projectionOperations = projectionOperations;
		this.voteProjectionOperations = voteProjectionOperations;
	}

	@Override
	protected Class<BookRequestAddedIntegrationPayload> payloadType() {
		return BookRequestAddedIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(BookRequestAddedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projectionOperations.add(getBookRequest(payload, event.occurredAt()));
		voteProjectionOperations.add(payload.requestId(), payload.requesterUserId(), event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.requestId()));
	}
	
	private BookRequestView getBookRequest(BookRequestAddedIntegrationPayload payload, Instant occurredAt) {
		return new BookRequestView(payload.requestId(), payload.requesterUserId(), payload.author(), payload.title(), payload.isbn(),
				payload.notes(), 1, null, BookRequestStatus.PENDING, occurredAt, occurredAt);
	}

}
