package mentoring.acomi.loanservice.domain.events;

import java.time.Instant;

import mentoring.acomi.loanservice.domain.events.payload.LoanPayload;

public record LoanConfirmRequestedEvent(
	    String aggregateId,
	    String eventId,
	    int eventVersion,
	    LoanPayload payload,
        Instant occurredAt
        )implements LoanProcessEvent {
    @Override public LoanEventType type() { return LoanEventType.LoanConfirmRequested; }
}