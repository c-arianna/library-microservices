package mentoring.acomi.userservice.application.eventhandler;

import java.util.function.Consumer;

import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.UserEventType;

public interface EventDispatcher {
	public void dispatch(UserEvent event);
	public void subscribe(UserEventType eventType, Consumer<UserEvent> callback);

}
