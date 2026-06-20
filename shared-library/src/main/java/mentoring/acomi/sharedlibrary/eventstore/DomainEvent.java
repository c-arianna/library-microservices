package mentoring.acomi.sharedlibrary.eventstore;

import java.time.Instant;

public interface DomainEvent {
	String aggregateId();
	String eventId();
	int eventVersion();
	Instant occurredAt();
	Object payload();
}
