package mentoring.acomi.assistantservice.infrastructure.persistence.repositories;

import mentoring.acomi.sharedcorelibrary.integration.messaging.NotificationEventRepository;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;

public interface AssistantNotificationRepository extends NotificationEventRepository<NotificationEventEnvelope<?>> {

}
