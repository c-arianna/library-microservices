package mentoring.acomi.bookservice.domain.events.bookrequest;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestPriceUpdatedPayload;

public record BookRequestPriceUpdatedEvent(
	    String aggregateId,
	    String eventId,
	    int eventVersion,
	    BookRequestPriceUpdatedPayload payload,
	    Instant occurredAt   
	) implements BookRequestEvent {
	    @Override public BookRequestEventType type() { return BookRequestEventType.BookRequestPriceUpdated; }
	}