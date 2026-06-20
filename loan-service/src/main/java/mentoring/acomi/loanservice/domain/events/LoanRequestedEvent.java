package mentoring.acomi.loanservice.domain.events;

import java.time.Instant;

import mentoring.acomi.loanservice.domain.events.payload.LoanRequestPayload;

public record LoanRequestedEvent (
	    String aggregateId,
	    String eventId,
	    int eventVersion,
        LoanRequestPayload payload,
        Instant occurredAt
        )implements LoanStateEvent {
    @Override public LoanEventType type() { return LoanEventType.LoanRequested; }
}