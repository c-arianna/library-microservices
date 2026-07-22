package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

public record NotificationHandlerKey(NotificationEventType eventType, int schemaVersion) {}