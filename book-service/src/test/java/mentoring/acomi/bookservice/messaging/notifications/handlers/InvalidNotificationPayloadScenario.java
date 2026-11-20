package mentoring.acomi.bookservice.messaging.notifications.handlers;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;

public record InvalidNotificationPayloadScenario(String description, String field, NotificationEventEnvelope<?> event) {

}
