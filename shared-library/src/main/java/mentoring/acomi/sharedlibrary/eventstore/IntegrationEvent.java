package mentoring.acomi.sharedlibrary.eventstore;

import java.time.Instant;

public interface IntegrationEvent {
	String aggregateId();
	String aggregateType();
	String eventId();
	int eventVersion();
	Instant occurredAt();
	Object payload();
}
