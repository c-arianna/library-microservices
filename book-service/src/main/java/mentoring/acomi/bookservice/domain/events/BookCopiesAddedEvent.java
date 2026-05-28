package mentoring.acomi.bookservice.domain.events;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.payload.BookCopiesAddedPayload;

public record BookCopiesAddedEvent (
	    String aggregateId,
	    String eventId,
        BookCopiesAddedPayload payload,
        Instant occurredAt
        )implements BookStateEvent {
    @Override public BookEventType type() { return BookEventType.BookCopiesAdded; }
}
