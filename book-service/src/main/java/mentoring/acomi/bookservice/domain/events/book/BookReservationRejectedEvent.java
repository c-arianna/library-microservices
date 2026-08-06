package mentoring.acomi.bookservice.domain.events.book;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.book.payload.BookReservationRejectedPayload;

public record BookReservationRejectedEvent(
		String aggregateId,
		String eventId,
		int eventVersion,
		BookReservationRejectedPayload payload,
        Instant occurredAt
        )implements BookProcessEvent {
    @Override public BookEventType type() { return BookEventType.BookReservationRejected; }
}