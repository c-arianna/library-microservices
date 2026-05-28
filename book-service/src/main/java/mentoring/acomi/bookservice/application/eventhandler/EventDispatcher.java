package mentoring.acomi.bookservice.application.eventhandler;

import java.util.function.Consumer;

import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.domain.events.BookEventType;

public interface EventDispatcher {
	public void dispatch(BookEvent event);
	public void subscribe(BookEventType eventType, Consumer<BookEvent> callback);

}
