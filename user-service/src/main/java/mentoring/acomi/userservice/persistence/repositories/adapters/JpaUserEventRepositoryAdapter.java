package mentoring.acomi.userservice.persistence.repositories.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserEventEntity;
import mentoring.acomi.userservice.infrastructure.persistence.mapper.UserEventJpaMapper;
import mentoring.acomi.userservice.infrastructure.persistence.repositories.UserEventJpaRepository;

@Repository
public class JpaUserEventRepositoryAdapter implements UserEventRepository {

	private final UserEventJpaRepository repository;
	private final UserEventJpaMapper mapper;

	public JpaUserEventRepositoryAdapter(UserEventJpaRepository repository, UserEventJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void appendToStream(UserEvent event) {

		Optional<Integer> version = repository.findLastVersion(event.aggregateId());

		Integer nextVersion = version.isEmpty() ? 0 : version.get() + 1;

		UserEventEntity entity = mapper.toEntity(event);
		entity.setEventVersion(nextVersion);

		repository.save(entity);
	}

	@Override
	public List<UserEvent> loadStream(String aggregateId) {
		return repository.findEventsForAggregate(aggregateId).stream().map(mapper::toDomain).toList();
	}

	@Override
	public boolean exists(String aggregateId) {
		return repository.existsByAggregateId(aggregateId);
	}

	@Override
	public List<UserEvent> loadAll() {
		return repository.findAll(Sort.by("eventVersion")).stream().map(mapper::toDomain).toList();
	}

	@Override
	public Optional<UserEvent> getEvent(String eventType, String aggregateId) {
		Optional<UserEventEntity> event = repository.getByEventTypeAndAggregateId(eventType, aggregateId);

		return event.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(event.get()));
	}

}
