package mentoring.acomi.bookservice.application.eventhandler;

import mentoring.acomi.bookservice.domain.events.BookEvent;

public interface EventDispatcher {
	public void dispatch(BookEvent event);
}
