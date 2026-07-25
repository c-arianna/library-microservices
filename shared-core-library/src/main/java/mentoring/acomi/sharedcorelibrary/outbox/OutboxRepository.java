package mentoring.acomi.sharedcorelibrary.outbox;

import java.time.Instant;
import java.util.List;

public interface OutboxRepository {
	void add(OutboxEvent outboxEvent);
	List<OutboxEvent> findEventsToPublish(Instant now, int limit);
	void published(String eventId, Instant publishedAt);
	void recordFailure(String eventId, OutboxStatus status, String lastError, int retryCount, Instant nextRetryAt);
	void deleteAll();
}