package mentoring.acomi.userservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

import mentoring.acomi.userservice.application.messaging.EventDispatcher;
import mentoring.acomi.userservice.application.projection.UserProjection;
import mentoring.acomi.userservice.domain.events.UserEvent;

@Component
public class CompositeEventDispatcher implements EventDispatcher {

	private final UserProjection projection;
	private final UserIntegrationEventPublisher publisher;

	public CompositeEventDispatcher(UserProjection projection, UserIntegrationEventPublisher publisher) {
		this.projection = projection;
		this.publisher = publisher;
	}

	@Override
	public void dispatch(UserEvent event) {
		projection.updateView(event);
		publisher.dispatch(event);
	}
}