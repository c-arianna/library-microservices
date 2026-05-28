package mentoring.acomi.bookservice.domain.events;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.payload.BookReservationRejectedPayload;

public record BookReservationRejectedEvent(
		String aggregateId,
		String eventId,
		BookReservationRejectedPayload payload,
        Instant occurredAt
        )implements BookProcessEvent {
    @Override public BookEventType type() { return BookEventType.BookReservationRejected; }
}