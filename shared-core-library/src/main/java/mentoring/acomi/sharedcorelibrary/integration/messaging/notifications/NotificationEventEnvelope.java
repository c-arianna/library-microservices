package mentoring.acomi.sharedcorelibrary.integration.messaging.notifications;

import java.time.Instant;

public record NotificationEventEnvelope<T>(String eventId, NotificationEventType eventType, String producer, 
		Instant occurredAt, int schemaVersion, T payload) {}
