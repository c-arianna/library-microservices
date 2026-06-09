package mentoring.acomi.bookservice.application.repositories;

import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.sharedlibrary.eventstore.EventRepository;

public interface BookEventRepository extends EventRepository<BookEvent>{
}
