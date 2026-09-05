package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.projection.BookRequestProjectionOperations;
import mentoring.acomi.bookservice.application.projection.BookRequestVoteProjectionOperations;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestVotedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.BOOK_REQUEST_VOTED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
@Component
public class BookRequestVotedV1Handler extends AbstractEventHandler<BookRequestVotedIntegrationPayload> {

	private final BookRequestProjectionOperations projectionOperations;
	private final BookRequestVoteProjectionOperations voteProjectionOperations;
	
	public BookRequestVotedV1Handler(@Qualifier("liveBookRequestProjection") BookRequestProjectionOperations projectionOperations, 
			@Qualifier("liveBookRequestVoteProjection") BookRequestVoteProjectionOperations voteProjectionOperations, EventPayloadMapper mapper) {
		super(mapper);
		this.projectionOperations = projectionOperations;
		this.voteProjectionOperations = voteProjectionOperations;
	}

	@Override
	protected Class<BookRequestVotedIntegrationPayload> payloadType() {
		return BookRequestVotedIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(BookRequestVotedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projectionOperations.registerVotes(payload.requestId(), 1, event.occurredAt());
		voteProjectionOperations.add(payload.requestId(), payload.userId(), event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.requestId()));
	}
}
