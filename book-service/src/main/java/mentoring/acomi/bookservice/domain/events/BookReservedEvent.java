package mentoring.acomi.bookservice.domain.events;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.payload.BookLoanPayload;

public record BookReservedEvent( 
		String aggregateId,
		String eventId,
	    BookLoanPayload payload,
        Instant occurredAt
        )implements BookStateEvent {
    @Override public BookEventType type() { return BookEventType.BookReserved; }
}
