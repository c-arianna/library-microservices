package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.DailyLoanStatisticProjectionOperations;
import mentoring.acomi.loanservice.application.projection.LoanProjectionOperations;
import mentoring.acomi.loanservice.application.projection.UserLoanStatisticProjectionOperations;
import mentoring.acomi.loanservice.application.projection.UserProjectionOperations;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanConfirmedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanFailedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanRequestedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanReservedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanReturnedHandler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LibraryCardAssignedHandler;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LoanCanceledV1Handler;
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
    private final UserLoanStatisticProjectionOperations userLoanStatisticReplayProjection;
    private final DailyLoanStatisticProjectionOperations dailyLoanStatisticReplayProjection;
    
	private final EventPayloadMapper mapper;

	public ReplayLoanHandlerFactory(@Qualifier("replayLoanProjection") LoanProjectionOperations loanReplayProjection, 
			@Qualifier("replayUserProjection") UserProjectionOperations userReplayProjection, 
			@Qualifier("replayUserLoanStatisticProjection") UserLoanStatisticProjectionOperations userLoanStatisticReplayProjection,
			@Qualifier("replayDailyLoanStatisticProjection") DailyLoanStatisticProjectionOperations dailyLoanStatisticReplayProjection,
			EventPayloadMapper mapper) {
		this.loanReplayProjection = loanReplayProjection;
		this.userReplayProjection = userReplayProjection;
		this.userLoanStatisticReplayProjection = userLoanStatisticReplayProjection;
		this.dailyLoanStatisticReplayProjection = dailyLoanStatisticReplayProjection;
		this.mapper = mapper;
	}

	public List<EventHandler> createHandlers() {

		return List.of(new LoanCanceledV1Handler(loanReplayProjection, dailyLoanStatisticReplayProjection, mapper),
				   	   new LoanConfirmedV1Handler(loanReplayProjection, dailyLoanStatisticReplayProjection, mapper),
				       new LoanFailedV1Handler(loanReplayProjection, mapper),
				       new LoanRequestedV1Handler(loanReplayProjection, dailyLoanStatisticReplayProjection, mapper),
				       new LoanReservedV1Handler(loanReplayProjection, mapper),
				       new LoanReturnedHandler(loanReplayProjection, userLoanStatisticReplayProjection, dailyLoanStatisticReplayProjection, 
				    		   mapper),
				       new UserSubscribedHandler(userReplayProjection, mapper),
				       new UserSuspendedV1Handler(userReplayProjection, mapper),
				       new UserUnsubscribedV1Handler(userReplayProjection, mapper),
				       new UserUnsuspendedV1Handler(userReplayProjection, mapper),
				       new LibraryCardAssignedHandler(userReplayProjection, mapper)
				       
		);
	}
}