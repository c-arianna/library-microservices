package mentoring.acomi.bookservice.domain.events;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.payload.BookLoanPayload;

public record BookReleasedEvent(
		String aggregateId,
		String eventId,
		 int eventVersion,
	    BookLoanPayload payload,
        Instant occurredAt
        )implements BookStateEvent {
    @Override public BookEventType type() { return BookEventType.BookReleased; }
}