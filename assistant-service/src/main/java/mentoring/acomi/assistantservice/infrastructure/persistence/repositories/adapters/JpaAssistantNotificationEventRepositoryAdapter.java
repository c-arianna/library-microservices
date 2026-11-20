package mentoring.acomi.assistantservice.infrastructure.persistence.repositories.adapters;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.assistantservice.infrastructure.persistence.entity.AssistantEventEntity;
import mentoring.acomi.assistantservice.infrastructure.persistence.repositories.AssistantEventJpaRepository;
import mentoring.acomi.assistantservice.infrastructure.persistence.repositories.AssistantNotificationRepository;
import mentoring.acomi.sharedcorelibrary.eventstore.EventMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedjpalibrary.eventstore.AbstractJpaNotificationEventRepositoryAdapter;

@Repository
@Transactional
public class JpaAssistantNotificationEventRepositoryAdapter extends AbstractJpaNotificationEventRepositoryAdapter<NotificationEventEnvelope<?>, AssistantEventEntity> implements AssistantNotificationRepository {
	
	protected JpaAssistantNotificationEventRepositoryAdapter(AssistantEventJpaRepository repository, EventMapper<NotificationEventEnvelope<?>, AssistantEventEntity> mapper) {
		super(repository, mapper);
	}

}
