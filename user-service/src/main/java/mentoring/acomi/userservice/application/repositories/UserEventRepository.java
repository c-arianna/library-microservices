package mentoring.acomi.userservice.application.repositories;

import java.util.List;

import mentoring.acomi.sharedcorelibrary.eventstore.EventRepository;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserEventEntity;

public interface UserEventRepository extends EventRepository<UserEvent>{
	List<UserEventEntity> findAllEvents();
}
