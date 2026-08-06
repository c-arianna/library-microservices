package mentoring.acomi.bookservice.domain.events.bookrequest;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestRejectedPayload;

public record BookRequestRejectedEvent( String aggregateId,
	    String eventId,
	    int eventVersion,
	    BookRequestRejectedPayload payload,
	    Instant occurredAt   
	) implements BookRequestEvent {
	    @Override public BookRequestEventType type() { return BookRequestEventType.BookRequestRejected; }
	}
