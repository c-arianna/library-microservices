package mentoring.acomi.bookservice.application.repositories;

import java.util.List;

import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.sharedcorelibrary.eventstore.EventRepository;

public interface BookEventRepository extends EventRepository<BookEvent>{
	List<BookEventEntity> findAllEvents();
}
