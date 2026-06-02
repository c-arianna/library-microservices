package mentoring.acomi.bookservice.infrastructure.persistence.repositories.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.mapper.BookEventJpaMapper;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookEventJpaRepository;

@Repository
@Transactional
public class JpaBookEventRepositoryAdapter implements BookEventRepository {

	private final BookEventJpaRepository repository;
	private final BookEventJpaMapper mapper;

	public JpaBookEventRepositoryAdapter(BookEventJpaRepository repository, BookEventJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void appendToStream(BookEvent event) {

		Optional<Integer> version = repository.findLastVersion(event.aggregateId());

		Integer nextVersion = version.isEmpty() ? 0 : version.get() + 1;

		BookEventEntity entity = mapper.toEntity(event);
		entity.setEventVersion(nextVersion);

		repository.save(entity);
	}

	@Override
	public List<BookEvent> loadStream(String aggregateId) {
		return repository.findEventsForAggregate(aggregateId).stream().map(mapper::toDomain).toList();
	}

	@Override
	public boolean exists(String aggregateId) {
		return repository.existsByAggregateId(aggregateId);
	}

	@Override
	public List<BookEvent> loadAll() {
		return repository.findAll(Sort.by("eventVersion")).stream().map(mapper::toDomain).toList();
	}

	@Override
	public Optional<BookEvent> getEvent(String eventType, String aggregateId) {
		Optional<BookEventEntity> event = repository.getByEventTypeAndAggregateId(eventType, aggregateId);

		return event.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(event.get()));
	}

}
