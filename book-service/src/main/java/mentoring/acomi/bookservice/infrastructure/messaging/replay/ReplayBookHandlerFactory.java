package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.projection.BookProjectionOperations;
import mentoring.acomi.bookservice.application.projection.BookRequestProjectionOperations;
import mentoring.acomi.bookservice.application.projection.BookRequestVoteProjectionOperations;
import mentoring.acomi.bookservice.application.projection.UserProjectionOperations;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookBorrowedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookCopiesUpdatedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookRegisteredV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookReleasedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookRequestAddedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookRequestApprovedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookRequestPriceUpdatedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookRequestRejectedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookRequestVotedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookReservedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookReturnedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.UserSubscribedHandler;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.UserUnsubscribedV1Handler;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;

@Component
public class ReplayBookHandlerFactory {

    private final BookProjectionOperations replayProjection;
    private final BookRequestProjectionOperations replayBookRequestProjection;
	private final BookRequestVoteProjectionOperations replayBookRequestVoteProjection;
	private final UserProjectionOperations replayUserProjection;
	private final EventPayloadMapper mapper;

	public ReplayBookHandlerFactory(@Qualifier("replayBookProjection") BookProjectionOperations replayProjection, 
			@Qualifier("replayBookRequestProjection") BookRequestProjectionOperations replayBookRequestProjection, 
			@Qualifier("replayBookRequestVoteProjection") BookRequestVoteProjectionOperations replayBookRequestVoteProjection,
			@Qualifier("replayUserProjection") UserProjectionOperations replayUserProjection,
			EventPayloadMapper mapper) {
		this.replayProjection = replayProjection;
		this.replayBookRequestProjection = replayBookRequestProjection;
		this.replayBookRequestVoteProjection = replayBookRequestVoteProjection;
		this.replayUserProjection = replayUserProjection;
		this.mapper = mapper;
	}

	public List<EventHandler> createHandlers() {

		return List.of(new BookBorrowedV1Handler(replayProjection, mapper),
				   	   new BookCopiesUpdatedV1Handler(replayProjection, mapper),
				       new BookRegisteredV1Handler(replayProjection, mapper),
				       new BookReleasedV1Handler(replayProjection, mapper),
				       new BookReservedV1Handler(replayProjection, mapper),
				       new BookReturnedV1Handler(replayProjection, mapper),
				       new BookRequestAddedV1Handler(replayBookRequestProjection, replayBookRequestVoteProjection, mapper),
				       new BookRequestApprovedV1Handler(replayBookRequestProjection, mapper),
				       new BookRequestRejectedV1Handler(replayBookRequestProjection, mapper),
				       new BookRequestVotedV1Handler(replayBookRequestProjection, replayBookRequestVoteProjection, mapper),
				       new BookRequestPriceUpdatedV1Handler(replayBookRequestProjection, mapper),
				       new UserSubscribedHandler(replayUserProjection, mapper),
				       new UserUnsubscribedV1Handler(replayUserProjection, mapper)				       
		);
	}
}