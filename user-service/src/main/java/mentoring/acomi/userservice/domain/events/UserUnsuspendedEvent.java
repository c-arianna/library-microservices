package mentoring.acomi.userservice.domain.events;

import java.time.Instant;

import mentoring.acomi.userservice.domain.events.payload.UserPayload;

public record UserUnsuspendedEvent(
		String aggregateId,
		String eventId,
		int eventVersion,
		UserPayload payload,
        Instant occurredAt
        )implements UserEvent {
    @Override public UserEventType type() { return UserEventType.UserUnsuspended; }
}