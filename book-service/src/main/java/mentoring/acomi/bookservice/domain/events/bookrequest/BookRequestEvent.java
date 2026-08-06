package mentoring.acomi.bookservice.domain.events.bookrequest;

import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.sharedcorelibrary.eventstore.DomainEvent;

public sealed interface BookRequestEvent extends DomainEvent permits BookRequestAddedEvent, BookRequestVotedEvent, BookRequestApprovedEvent,
BookRequestRejectedEvent {
	BookRequestEventType type();
	@Override public default String aggregateType() { return AggregateType.BOOK_REQUEST.name();}

}
