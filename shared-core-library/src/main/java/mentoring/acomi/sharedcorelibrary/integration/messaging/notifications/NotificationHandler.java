package mentoring.acomi.sharedcorelibrary.integration.messaging.notifications;

public interface NotificationHandler {
    void handleEvent(NotificationEventEnvelope<?> event);
}