package mentoring.acomi.loanservice.application.eventhandler;

import java.util.function.Consumer;

import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanEventType;

public interface EventDispatcher {
	public void dispatch(LoanEvent event);
	public void subscribe(LoanEventType eventType, Consumer<LoanEvent> callback);

}
