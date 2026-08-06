package mentoring.acomi.bookservice.domain.events.bookrequest;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestApprovedPayload;

public record BookRequestApprovedEvent(String aggregateId,
	    String eventId,
	    int eventVersion,
	    BookRequestApprovedPayload payload,
	    Instant occurredAt   
	) implements BookRequestEvent {
	    @Override public BookRequestEventType type() { return BookRequestEventType.BookRequestApproved; }
	}