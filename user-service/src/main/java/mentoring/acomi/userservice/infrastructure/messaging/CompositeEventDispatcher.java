package mentoring.acomi.userservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

import mentoring.acomi.userservice.application.messaging.EventDispatcher;
import mentoring.acomi.userservice.domain.events.UserEvent;

@Component
public class CompositeEventDispatcher implements EventDispatcher {

	private final UserIntegrationEventPublisher publisher;

	public CompositeEventDispatcher(UserIntegrationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void dispatch(UserEvent event) {
		publisher.dispatch(event);
	}
}