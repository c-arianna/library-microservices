package mentoring.acomi.assistantservice.application.projection;

import java.time.Instant;

public interface ProjectionOperations<T> {
	void upsert(T view, Instant eventOccurredAt);
}
