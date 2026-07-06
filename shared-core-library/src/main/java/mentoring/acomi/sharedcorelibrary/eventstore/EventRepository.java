package mentoring.acomi.sharedcorelibrary.eventstore;

import java.util.List;
import java.util.Optional;

public interface EventRepository<E extends DomainEvent> {
	public void appendToStream(E event, int schemaVersion);
	public List<E> loadStream(String aggregateId);
	public boolean exists(String aggregateId, String aggregateType);
	public Optional<E> getEvent(String eventType, String aggregateId);
	public void deleteAll();
    public Optional<Integer> findMaxProcessedVersion(String aggregateId, String aggregateType);
    public void markProcessed(String eventId, String aggregateType);
    Optional<E> findNextEventToProcess(String aggregateId,  String aggregateType, int eventVersion);
    public boolean existsEventProcessed(String eventId, String aggregateType);
}
