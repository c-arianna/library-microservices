package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;

public interface NotificationHandler {
    void handleEvent(NotificationEventEnvelope<?> event);
}