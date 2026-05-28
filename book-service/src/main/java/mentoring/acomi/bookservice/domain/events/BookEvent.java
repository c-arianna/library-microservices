package mentoring.acomi.bookservice.domain.events;

import java.time.Instant;

public sealed interface BookEvent permits BookStateEvent, BookProcessEvent{
	String aggregateId();
	String eventId();
	Instant occurredAt();
	BookEventType type();
	Object payload();
}

