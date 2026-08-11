package mentoring.acomi.userservice.application.repositories;

import java.util.List;

import mentoring.acomi.sharedcorelibrary.eventstore.EventRepository;
import mentoring.acomi.userservice.domain.events.AggregateType;
import mentoring.acomi.userservice.domain.events.UserEvent;

public interface UserEventRepository extends EventRepository<UserEvent>{
		
	default List<UserEvent> loadStream(String aggregateId) {
		return loadStream(aggregateId, AggregateType.USER.name());
	}
}
