package mentoring.acomi.userservice.domain.events;

import java.time.Instant;

public sealed interface UserEvent permits UserSubscribedEvent, UserUnsubscribeEvent, UserSuspendEvent,UserUnsuspendedEvent {
	String aggregateId();
	String eventId();
	Instant occurredAt();
	UserEventType type();
	Object payload();
}
