package mentoring.acomi.sharedlibrary.eventstore;

import java.util.List;
import java.util.Optional;

public abstract class AbstractJpaEventRepositoryAdapter<E extends DomainEvent, ENTITY extends BaseEventEntity>
		implements EventRepository<E> {

	private final BaseEventJpaRepository<ENTITY> repository;
	private final EventMapper<E, ENTITY> mapper;

	protected AbstractJpaEventRepositoryAdapter(BaseEventJpaRepository<ENTITY> repository, EventMapper<E, ENTITY> mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void appendToStream(E event) {
		ENTITY entity = mapper.toEntity(event);
		repository.save(entity);
	}

	@Override
	public List<E> loadStream(String aggregateId) {
		return repository.findEventsForAggregate(aggregateId).stream().map(mapper::toDomain).toList();
	}

	@Override
	public boolean exists(String aggregateId) {
		return repository.existsByAggregateId(aggregateId);
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
}
