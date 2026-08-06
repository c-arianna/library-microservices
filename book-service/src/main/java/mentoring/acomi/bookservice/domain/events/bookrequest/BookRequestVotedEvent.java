package mentoring.acomi.bookservice.domain.events.bookrequest;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestVotedPayload;

public record BookRequestVotedEvent(String aggregateId,
	    String eventId,
	    int eventVersion,
	    BookRequestVotedPayload payload,
	    Instant occurredAt   
	) implements BookRequestEvent {
	    @Override public BookRequestEventType type() { return BookRequestEventType.BookRequestVoted; }
	}