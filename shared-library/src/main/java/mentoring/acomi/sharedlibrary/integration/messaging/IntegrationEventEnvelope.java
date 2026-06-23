package mentoring.acomi.sharedlibrary.integration.messaging;

import java.time.Instant;

import mentoring.acomi.sharedlibrary.eventstore.IntegrationEvent;

public record IntegrationEventEnvelope<T>(String eventId, IntegrationEventTypes eventType, String producer, String aggregateId, String aggregateType, int eventVersion, 
		Instant occurredAt, int schemaVersion, T payload) implements IntegrationEvent {
}