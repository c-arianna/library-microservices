package mentoring.acomi.assistantservice.messaging.notifications.handlers;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;

public record InvalidNotificationPayloadScenario(String description, String field, NotificationEventEnvelope<?> event) {

}
