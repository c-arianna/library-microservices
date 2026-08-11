package mentoring.acomi.bookservice.application.repositories;

import java.util.List;

import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.domain.events.book.BookEvent;
import mentoring.acomi.sharedcorelibrary.eventstore.EventRepository;

public interface BookEventRepository extends EventRepository<BookEvent> {
	
	default List<BookEvent> loadStream(String aggregateId) {
		return loadStream(aggregateId, AggregateType.BOOK.name());
	}
}
