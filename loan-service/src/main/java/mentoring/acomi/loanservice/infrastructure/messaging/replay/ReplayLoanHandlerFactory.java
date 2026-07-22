package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.LoanProjectionOperations;
import mentoring.acomi.loanservice.application.projection.UserProjectionOperations;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanCanceledV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanConfirmedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanFailedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanRequestedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanReservedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanReturnedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LibraryCardAssignedHandler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.UserSubscribedHandler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.UserSuspendedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.UserUnsubscribedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.UserUnsuspendedV1Handler;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;

@Component
public class ReplayLoanHandlerFactory {

    private final LoanProjectionOperations loanReplayProjection;
    private final UserProjectionOperations userReplayProjection;
	private final EventPayloadMapper mapper;

	public ReplayLoanHandlerFactory(@Qualifier("replayLoanProjection") LoanProjectionOperations loanReplayProjection, 
			@Qualifier("replayUserProjection") UserProjectionOperations userReplayProjection, EventPayloadMapper mapper) {
		this.loanReplayProjection = loanReplayProjection;
		this.userReplayProjection = userReplayProjection;
		this.mapper = mapper;
	}

	public List<EventHandler> createHandlers() {

		return List.of(new LoanCanceledV1Handler(loanReplayProjection, mapper),
				   	   new LoanConfirmedV1Handler(loanReplayProjection, mapper),
				       new LoanFailedV1Handler(loanReplayProjection, mapper),
				       new LoanRequestedV1Handler(loanReplayProjection, mapper),
				       new LoanReservedV1Handler(loanReplayProjection, mapper),
				       new LoanReturnedV1Handler(loanReplayProjection, mapper),
				       new UserSubscribedHandler(userReplayProjection, mapper),
				       new UserSuspendedV1Handler(userReplayProjection, mapper),
				       new UserUnsubscribedV1Handler(userReplayProjection, mapper),
				       new UserUnsuspendedV1Handler(userReplayProjection, mapper),
				       new LibraryCardAssignedHandler(userReplayProjection, mapper)
				       
		);
	}
}