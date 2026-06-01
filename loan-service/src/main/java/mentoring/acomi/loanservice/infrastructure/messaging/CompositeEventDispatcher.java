package mentoring.acomi.loanservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.messaging.EventDispatcher;
import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanStateEvent;

@Component
public class CompositeEventDispatcher implements EventDispatcher {

	private final LoanProjection projection;
	private final LoanIntegrationEventPublisher publisher;

	public CompositeEventDispatcher(LoanProjection projection, LoanIntegrationEventPublisher publisher) {
		this.projection = projection;
		this.publisher = publisher;
	}

	@Override
	public void dispatch(LoanEvent event) {

		if (event instanceof LoanStateEvent stateEvent) {
			projection.updateView(stateEvent);
		}

		publisher.dispatch(event);
	}

}
