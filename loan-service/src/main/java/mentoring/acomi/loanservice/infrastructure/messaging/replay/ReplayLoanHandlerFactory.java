package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.ProjectionDispatcher;
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

    private final ProjectionDispatcher replayDispatcher;
        
	private final EventPayloadMapper mapper;

	public ReplayLoanHandlerFactory(@Qualifier("replayDispatcher") ProjectionDispatcher replayDispatcher, EventPayloadMapper mapper) {
		this.replayDispatcher = replayDispatcher;
		this.mapper = mapper;
	}

	public List<EventHandler> createHandlers() {

		return List.of(new LoanCanceledV1Handler(replayDispatcher, mapper),
				   	   new LoanConfirmedV1Handler(replayDispatcher, mapper),
				       new LoanFailedV1Handler(replayDispatcher, mapper),
				       new LoanRequestedV1Handler(replayDispatcher, mapper),
				       new LoanReservedV1Handler(replayDispatcher, mapper),
				       new LoanReturnedHandler(replayDispatcher, mapper),
				       new UserSubscribedHandler(replayDispatcher, mapper),
				       new UserSuspendedV1Handler(replayDispatcher, mapper),
				       new UserUnsubscribedV1Handler(replayDispatcher, mapper),
				       new UserUnsuspendedV1Handler(replayDispatcher, mapper),
				       new LibraryCardAssignedHandler(replayDispatcher, mapper)
				       
		);
	}
	
}