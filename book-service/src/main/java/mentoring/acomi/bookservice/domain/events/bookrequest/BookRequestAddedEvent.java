package mentoring.acomi.bookservice.domain.events.bookrequest;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestAddedPayload;

public record BookRequestAddedEvent(
	    String aggregateId,
	    String eventId,
	    int eventVersion,
	    BookRequestAddedPayload payload,
	    Instant occurredAt   
	) implements BookRequestEvent {
	    @Override public BookRequestEventType type() { return BookRequestEventType.BookRequestAdded; }
	}