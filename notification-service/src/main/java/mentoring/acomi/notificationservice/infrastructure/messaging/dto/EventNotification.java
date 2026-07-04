package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

public record EventNotification(IntegrationEventTypes eventType, EventNotificationPayload payload ) {}
