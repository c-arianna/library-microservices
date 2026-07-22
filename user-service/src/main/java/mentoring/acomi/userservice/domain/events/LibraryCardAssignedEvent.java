package mentoring.acomi.userservice.domain.events;

import java.time.Instant;

import mentoring.acomi.userservice.domain.events.payload.LibraryCardAssignedPayload;

public record LibraryCardAssignedEvent(
		String aggregateId,
        String eventId,
        int eventVersion,
        LibraryCardAssignedPayload payload,
        Instant occurredAt
) implements UserEvent {

    @Override
    public UserEventType type() {
        return UserEventType.LibraryCardAssigned;
    }
}