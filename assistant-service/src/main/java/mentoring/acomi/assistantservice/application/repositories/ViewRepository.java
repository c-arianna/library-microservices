package mentoring.acomi.assistantservice.application.repositories;

import java.time.Instant;

public interface ViewRepository<T> {
	void upsert(T view, Instant eventOccurredAt);
}
