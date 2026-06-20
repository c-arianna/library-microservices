package mentoring.acomi.loanservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.messaging.EventDispatcher;
import mentoring.acomi.loanservice.domain.events.LoanEvent;

@Component
public class CompositeEventDispatcher implements EventDispatcher {

	private final LoanIntegrationEventPublisher publisher;

	public CompositeEventDispatcher(LoanIntegrationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void dispatch(LoanEvent event) {
		publisher.dispatch(event);
	}

}
