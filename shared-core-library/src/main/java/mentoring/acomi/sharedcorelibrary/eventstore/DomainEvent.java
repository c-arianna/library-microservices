package mentoring.acomi.sharedcorelibrary.eventstore;

import java.time.Instant;

public interface DomainEvent {
	String aggregateId();
	String aggregateType();
	String eventId();
	int eventVersion();
	Instant occurredAt();
	Object payload();
}
