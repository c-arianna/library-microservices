package mentoring.acomi.userservice.domain.events;

import mentoring.acomi.sharedcorelibrary.eventstore.DomainEvent;

public sealed interface UserEvent extends DomainEvent permits UserSubscribedEvent, UserUnsubscribeEvent, UserSuspendEvent,UserUnsuspendedEvent {
	UserEventType type();
	@Override public default String aggregateType() { return AggregateType.USER.name(); }
}
