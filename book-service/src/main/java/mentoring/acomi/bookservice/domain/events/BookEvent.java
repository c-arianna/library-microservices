package mentoring.acomi.bookservice.domain.events;

import mentoring.acomi.sharedlibrary.eventstore.DomainEvent;

public sealed interface BookEvent extends DomainEvent permits BookStateEvent, BookProcessEvent{
	BookEventType type();
	@Override public default String aggregateType() { return AggregateType.BOOK.name(); }
}

