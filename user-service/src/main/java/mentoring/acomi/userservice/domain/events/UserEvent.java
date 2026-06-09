package mentoring.acomi.userservice.domain.events;

import mentoring.acomi.sharedlibrary.eventstore.DomainEvent;

public sealed interface UserEvent extends DomainEvent permits UserSubscribedEvent, UserUnsubscribeEvent, UserSuspendEvent,UserUnsuspendedEvent {
	UserEventType type();
}
