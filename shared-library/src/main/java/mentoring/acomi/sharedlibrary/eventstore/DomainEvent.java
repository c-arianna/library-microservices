package mentoring.acomi.sharedlibrary.eventstore;

import java.time.Instant;

public interface DomainEvent {
	String aggregateId();
	String eventId();
	Instant occurredAt();
	Object payload();

}
