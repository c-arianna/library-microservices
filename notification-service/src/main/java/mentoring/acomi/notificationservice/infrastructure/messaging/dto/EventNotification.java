package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

public record EventNotification(NotificationEventType eventType, EventNotificationPayload payload ) {}
