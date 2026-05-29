package mentoring.acomi.userservice.application.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.userservice.domain.events.UserEvent;

public interface UserEventRepository {
	public void appendToStream(UserEvent event);
	public List<UserEvent> loadStream(String aggregateId);
	public boolean exists(String aggregateId);
	public List<UserEvent> loadAll();
	public Optional<UserEvent> getEvent(String eventType, String aggregateId);
}
