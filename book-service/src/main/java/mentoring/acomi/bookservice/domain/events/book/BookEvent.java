package mentoring.acomi.bookservice.domain.events.book;

import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.sharedcorelibrary.eventstore.DomainEvent;

public sealed interface BookEvent extends DomainEvent permits BookStateEvent, BookProcessEvent{
	BookEventType type();
	@Override public default String aggregateType() { return AggregateType.BOOK.name(); }
}

