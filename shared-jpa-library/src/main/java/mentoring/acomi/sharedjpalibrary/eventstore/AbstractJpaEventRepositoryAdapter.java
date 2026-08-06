package mentoring.acomi.sharedjpalibrary.eventstore;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.sharedcorelibrary.eventstore.DomainEvent;
import mentoring.acomi.sharedcorelibrary.eventstore.EventCategory;
import mentoring.acomi.sharedcorelibrary.eventstore.EventMapper;
import mentoring.acomi.sharedcorelibrary.eventstore.EventRepository;

public abstract class AbstractJpaEventRepositoryAdapter<E extends DomainEvent, ENTITY extends BaseEventEntity>
		implements EventRepository<E> {

	protected final BaseEventJpaRepository<ENTITY> repository;
	private final EventMapper<E, ENTITY> mapper;

	protected AbstractJpaEventRepositoryAdapter(BaseEventJpaRepository<ENTITY> repository, EventMapper<E, ENTITY> mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void appendToStream(E event, int schemaVersion) {
		ENTITY entity = mapper.toEntity(event);
		entity.setEventCategory(EventCategory.PRODUCER.name());
		entity.setSchemaVersion(schemaVersion);
		repository.save(entity);
	}

	@Override
	public List<E> loadStream(String aggregateId, String aggregateType) {
		return repository.findByAggregateIdAndAggregateTypeAndEventCategoryOrderByEventVersion(aggregateId, aggregateType, EventCategory.PRODUCER.name()).stream().map(mapper::toDomain).toList();
	}

	@Override
	public boolean exists(String aggregateId, String aggregateType) {
		return repository.existsByAggregateIdAndAggregateType(aggregateId, aggregateType);
	}

	@Override
	public Optional<E> getEvent(String eventType, String aggregateId) {
		Optional<ENTITY> event = repository.getByEventTypeAndAggregateId(eventType, aggregateId);
		return event.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(event.get()));
	}
	
	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
		
	@Override
	public void markProcessed(String eventId, String aggregateType) {
		repository.markProcessed(eventId, aggregateType);
	}
		
	@Override
	public boolean existsEventProcessed(String eventId, String aggregateType) {
		return repository.existsByEventIdAndAggregateTypeAndProcessedTrue(eventId, aggregateType);
	}
	
	@Override
	public Optional<E> getEventByEventIdAndAggregateType(String eventId, String aggregateType) {
		Optional<ENTITY> event = repository.getByEventIdAndAggregateType(eventId, aggregateType);
		return event.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(event.get()));
	}
}
