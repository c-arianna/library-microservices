package mentoring.acomi.loanservice.domain.events;

import java.time.Instant;

import mentoring.acomi.loanservice.domain.events.payload.LoanPayload;

public record LoanConfirmedEvent(
		String aggregateId,
		String eventId,
	    LoanPayload payload,
        Instant occurredAt
        )implements LoanStateEvent {
    @Override public LoanEventType type() { return LoanEventType.LoanConfirmed; }

}
