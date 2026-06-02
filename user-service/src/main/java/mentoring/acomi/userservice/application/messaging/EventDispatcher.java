package mentoring.acomi.userservice.application.messaging;

import mentoring.acomi.userservice.domain.events.UserEvent;

public interface EventDispatcher {
	public void dispatch(UserEvent event);
}
