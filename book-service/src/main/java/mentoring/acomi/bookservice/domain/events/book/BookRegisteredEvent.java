package mentoring.acomi.bookservice.domain.events.book;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.book.payload.BookRegisteredPayload;

public record BookRegisteredEvent(
    String aggregateId,
    String eventId,
    int eventVersion,
    BookRegisteredPayload payload,
    Instant occurredAt   
) implements BookStateEvent {
    @Override public BookEventType type() { return BookEventType.BookRegistered; }
}

