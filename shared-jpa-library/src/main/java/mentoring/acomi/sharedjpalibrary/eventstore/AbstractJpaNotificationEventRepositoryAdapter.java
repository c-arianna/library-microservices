package mentoring.acomi.sharedjpalibrary.eventstore;

import mentoring.acomi.sharedcorelibrary.eventstore.EventCategory;
import mentoring.acomi.sharedcorelibrary.eventstore.EventMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.NotificationEventRepository;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEvent;

public abstract class AbstractJpaNotificationEventRepositoryAdapter <E extends NotificationEvent, ENTITY extends BaseEventEntity> implements NotificationEventRepository<E> {

	private final BaseEventJpaRepository<ENTITY> repository;
	private final EventMapper<E, ENTITY> mapper;

	protected AbstractJpaNotificationEventRepositoryAdapter(BaseEventJpaRepository<ENTITY> repository, EventMapper<E, ENTITY> mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void save(E event) {
		ENTITY entity = mapper.toEntity(event);
		entity.setEventCategory(EventCategory.CONSUMER.name());
		repository.save(entity);
	}

	@Override
	public boolean exists(String eventId) {	
		return repository.existsByEventId(eventId);
	}
	
	@Override
	public void markProcessed(String eventId, String aggregateType) {
		repository.markProcessed(eventId, aggregateType);
	}
}
