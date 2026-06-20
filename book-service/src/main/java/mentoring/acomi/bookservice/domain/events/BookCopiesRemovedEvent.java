package mentoring.acomi.bookservice.domain.events;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.payload.BookCopiesRemovedPayload;

public record BookCopiesRemovedEvent(
	    String aggregateId,
	    String eventId,
	    int eventVersion,
	    BookCopiesRemovedPayload payload,
        Instant occurredAt
        )implements BookStateEvent {
    @Override public BookEventType type() { return BookEventType.BookCopiesRemoved; }
}
