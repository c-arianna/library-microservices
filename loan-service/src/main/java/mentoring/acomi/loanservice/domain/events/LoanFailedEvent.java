package mentoring.acomi.loanservice.domain.events;

import java.time.Instant;

import mentoring.acomi.loanservice.domain.events.payload.LoanFailedPayload;

public record LoanFailedEvent(
	    String aggregateId,
	    String eventId,
	    LoanFailedPayload payload,
        Instant occurredAt
        )implements LoanStateEvent {
    @Override public LoanEventType type() { return LoanEventType.LoanFailed; }
}
