package mentoring.acomi.notificationservice.infrastructure.messaging.handlers;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

public interface NotificationHandler {
    NotificationEventType eventType();
    boolean accepts(NotificationEventEnvelope<?> event);
    void handleEvent(NotificationEventEnvelope<?> event);
}