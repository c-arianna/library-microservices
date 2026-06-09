package mentoring.acomi.bookservice.domain.events;

import mentoring.acomi.sharedlibrary.eventstore.DomainEvent;

public sealed interface BookEvent extends DomainEvent permits BookStateEvent, BookProcessEvent{
	BookEventType type();
}

