package mentoring.acomi.sharedcorelibrary.outbox;

import java.time.Instant;

public record OutboxEvent(String eventId, String aggregateType, OutboxStatus status, int retryCount, 
		String lastError, Instant createdAt, Instant publishedAt, Instant nextRetryAt) {}