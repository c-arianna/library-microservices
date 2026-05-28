package mentoring.acomi.bookservice.domain.events;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.payload.BookBorrowRejectedPayload;

public record BookBorrowRejectedEvent(
		String aggregateId,
		String eventId,
		BookBorrowRejectedPayload payload,
        Instant occurredAt
        )implements BookProcessEvent {
    @Override public BookEventType type() { return BookEventType.BookBorrowRejected; }
}
