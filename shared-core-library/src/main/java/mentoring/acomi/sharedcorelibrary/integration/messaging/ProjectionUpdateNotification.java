package mentoring.acomi.sharedcorelibrary.integration.messaging;

public record ProjectionUpdateNotification(String aggregateId, int schemaVersion) {}