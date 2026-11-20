package mentoring.acomi.sharedcorelibrary.integration.messaging.notifications;

import mentoring.acomi.sharedcorelibrary.eventstore.StoredEvent;

public interface NotificationEvent extends StoredEvent {
    NotificationEventType eventType();
    String producer();
    int schemaVersion();
}
