package mentoring.acomi.loanservice.domain.events;

import java.time.Instant;

import mentoring.acomi.loanservice.domain.events.payload.LoanReturnedPayload;

public record LoanReturnedEvent(
		String aggregateId,
		String eventId,
		int eventVersion,
		LoanReturnedPayload payload,
        Instant occurredAt
        )implements LoanStateEvent {
    @Override public LoanEventType type() { return LoanEventType.LoanReturned; }

}