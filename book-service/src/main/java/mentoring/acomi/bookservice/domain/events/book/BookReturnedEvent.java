package mentoring.acomi.bookservice.domain.events.book;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.book.payload.BookLoanPayload;

public record BookReturnedEvent(
		String aggregateId,
		String eventId,
		int eventVersion,
	    BookLoanPayload payload,
        Instant occurredAt
        )implements BookStateEvent {
    @Override public BookEventType type() { return BookEventType.BookReturned; }
}