package mentoring.acomi.loanservice.domain.events;

import mentoring.acomi.sharedlibrary.eventstore.DomainEvent;


public sealed interface LoanEvent extends DomainEvent permits LoanStateEvent, LoanProcessEvent{
	LoanEventType type();
}
