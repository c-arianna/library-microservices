package mentoring.acomi.loanservice.domain.events;

import java.time.Instant;


public sealed interface LoanEvent permits LoanStateEvent, LoanProcessEvent{
	String aggregateId();
	String eventId();
	Instant occurredAt();
	LoanEventType type();
	Object payload();
}
