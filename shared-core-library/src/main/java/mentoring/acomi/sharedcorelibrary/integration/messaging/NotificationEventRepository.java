package mentoring.acomi.sharedcorelibrary.integration.messaging;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEvent;

public interface NotificationEventRepository<E extends NotificationEvent> {
    void save(E event);
    boolean exists(String eventId);
    void markProcessed(String eventId, String aggregateType);
}