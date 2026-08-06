package mentoring.acomi.bookservice.application.messaging;

import mentoring.acomi.bookservice.domain.events.book.BookEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestEvent;

public interface EventDispatcher {
	public void dispatch(BookEvent event);
	public void dispatch(BookRequestEvent event);
}
