package mentoring.acomi.sharedlibrary.integration.messaging;

import java.time.Instant;

public record IntegrationEventEnvelope<T>(String eventId, IntegrationEventTypes eventType, String producer, String aggregateId, Instant occurredAt, T payload) {}