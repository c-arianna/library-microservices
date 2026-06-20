package mentoring.acomi.userservice.domain.events;

import java.time.Instant;

import mentoring.acomi.userservice.domain.events.payload.UserSubscribedPayload;

public record UserSubscribedEvent(
		String aggregateId,
		String eventId,
		 int eventVersion,
	    UserSubscribedPayload payload,
        Instant occurredAt
        )implements UserEvent {
    @Override public UserEventType type() { return UserEventType.UserSubscribed; }
}