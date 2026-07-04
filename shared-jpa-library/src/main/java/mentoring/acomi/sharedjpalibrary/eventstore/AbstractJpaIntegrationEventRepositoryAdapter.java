package mentoring.acomi.sharedjpalibrary.eventstore;

import mentoring.acomi.sharedcorelibrary.eventstore.EventCategory;
import mentoring.acomi.sharedcorelibrary.eventstore.EventMapper;
import mentoring.acomi.sharedcorelibrary.eventstore.IntegrationEvent;
import mentoring.acomi.sharedcorelibrary.eventstore.IntegrationEventRepository;

public abstract class AbstractJpaIntegrationEventRepositoryAdapter<E extends IntegrationEvent, ENTITY extends BaseEventEntity>
		implements IntegrationEventRepository<E> {

	protected final BaseEventJpaRepository<ENTITY> repository;
	private final EventMapper<E, ENTITY> mapper;

	protected AbstractJpaIntegrationEventRepositoryAdapter(BaseEventJpaRepository<ENTITY> repository, EventMapper<E, ENTITY> mapper) {
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
	public boolean exists(String eventId, String aggregateType) {
		return repository.existsByEventIdAndAggregateTypeAndEventCategory(eventId, aggregateType, EventCategory.CONSUMER.name());
	}

}
