package mentoring.acomi.bookservice.application.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.bookservice.domain.events.BookEvent;

public interface BookEventRepository {
	public void appendToStream(BookEvent event);
	public List<BookEvent> loadStream(String aggregateId);
	public boolean exists(String aggregateId);
	public List<BookEvent> loadAll();
	public Optional<BookEvent> getEvent(String eventType, String aggregateId);
}
