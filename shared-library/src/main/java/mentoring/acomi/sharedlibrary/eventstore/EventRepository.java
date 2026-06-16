package mentoring.acomi.sharedlibrary.eventstore;

import java.util.List;
import java.util.Optional;

public interface EventRepository<E extends DomainEvent> {
	public void appendToStream(E event);
	public List<E> loadStream(String aggregateId);
	public boolean exists(String aggregateId);
	public Optional<E> getEvent(String eventType, String aggregateId);
	public void deleteAll();
}
