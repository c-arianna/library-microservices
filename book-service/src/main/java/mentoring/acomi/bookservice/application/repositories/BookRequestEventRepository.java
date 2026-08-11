package mentoring.acomi.bookservice.application.repositories;

import java.util.List;

import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestEvent;
import mentoring.acomi.sharedcorelibrary.eventstore.EventRepository;

public interface BookRequestEventRepository extends EventRepository<BookRequestEvent> {
		
	default List<BookRequestEvent> loadStream(String aggregateId) {
		return loadStream(aggregateId, AggregateType.BOOK_REQUEST.name());
	}
}
