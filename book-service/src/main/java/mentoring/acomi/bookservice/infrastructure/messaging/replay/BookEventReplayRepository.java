package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import java.util.List;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;

public interface BookEventReplayRepository {
	List<BookEventEntity> findAllEvents();
}
