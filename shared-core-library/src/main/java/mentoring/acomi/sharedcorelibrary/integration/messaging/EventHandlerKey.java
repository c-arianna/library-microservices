package mentoring.acomi.sharedcorelibrary.integration.messaging;

public record EventHandlerKey(IntegrationEventTypes eventType, int schemaVersion) {}