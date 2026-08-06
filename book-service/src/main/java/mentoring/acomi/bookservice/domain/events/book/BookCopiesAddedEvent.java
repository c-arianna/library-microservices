package mentoring.acomi.bookservice.domain.events.book;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.book.payload.BookCopiesAddedPayload;

public record BookCopiesAddedEvent (
	    String aggregateId,
	    String eventId,
	    int eventVersion,
        BookCopiesAddedPayload payload,
        Instant occurredAt
        )implements BookStateEvent {
    @Override public BookEventType type() { return BookEventType.BookCopiesAdded; }
}
