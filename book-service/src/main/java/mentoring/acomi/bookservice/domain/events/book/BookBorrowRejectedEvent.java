package mentoring.acomi.bookservice.domain.events.book;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.book.payload.BookBorrowRejectedPayload;

public record BookBorrowRejectedEvent(
		String aggregateId,
		String eventId,
		int eventVersion,
		BookBorrowRejectedPayload payload,
        Instant occurredAt
        )implements BookProcessEvent {
    @Override public BookEventType type() { return BookEventType.BookBorrowRejected; }
}
