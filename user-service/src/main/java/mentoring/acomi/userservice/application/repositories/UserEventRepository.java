package mentoring.acomi.userservice.application.repositories;

import java.util.List;

import mentoring.acomi.sharedcorelibrary.eventstore.EventRepository;
import mentoring.acomi.userservice.domain.events.AggregateType;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserEventEntity;

public interface UserEventRepository extends EventRepository<UserEvent>{
	List<UserEventEntity> findAllEvents();
	
	default List<UserEvent> loadStream(String aggregateId) {
		return loadStream(aggregateId, AggregateType.USER.name());
	}
}
