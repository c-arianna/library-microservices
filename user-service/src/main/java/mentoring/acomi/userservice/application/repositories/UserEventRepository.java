package mentoring.acomi.userservice.application.repositories;

import mentoring.acomi.sharedlibrary.eventstore.EventRepository;
import mentoring.acomi.userservice.domain.events.UserEvent;

public interface UserEventRepository extends EventRepository<UserEvent>{
}
