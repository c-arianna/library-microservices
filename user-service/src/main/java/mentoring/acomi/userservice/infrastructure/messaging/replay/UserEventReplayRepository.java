package mentoring.acomi.userservice.infrastructure.messaging.replay;

import java.util.List;

import mentoring.acomi.userservice.infrastructure.persistence.entity.UserEventEntity;

public interface UserEventReplayRepository {
	List<UserEventEntity> findAllEvents();
}
