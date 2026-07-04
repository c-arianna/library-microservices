package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;

public record EventNotification(IntegrationEventTypes eventType, EventNotificationPayload payload ) {}
