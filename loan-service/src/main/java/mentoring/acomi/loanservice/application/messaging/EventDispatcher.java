package mentoring.acomi.loanservice.application.messaging;

import mentoring.acomi.loanservice.domain.events.LoanEvent;

public interface EventDispatcher {
	public void dispatch(LoanEvent event);
}
