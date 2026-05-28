package mentoring.acomi.bookservice.domain.events;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.payload.BookRegisteredPayload;

public record BookRegisteredEvent(
    String aggregateId,
    String eventId,
    BookRegisteredPayload payload,
    Instant occurredAt   
) implements BookStateEvent {
    @Override public BookEventType type() { return BookEventType.BookRegistered; }
}

