package mentoring.acomi.userservice.domain.events;

import java.time.Instant;

import mentoring.acomi.userservice.domain.events.payload.UserUnsubscribedPayload;

public record UserUnsubscribeEvent(
		String aggregateId,
		String eventId,
		int eventVersion,
	    UserUnsubscribedPayload payload,
        Instant occurredAt) implements UserEvent {
    @Override public UserEventType type() { return UserEventType.UserUnsubscribed; }
}
