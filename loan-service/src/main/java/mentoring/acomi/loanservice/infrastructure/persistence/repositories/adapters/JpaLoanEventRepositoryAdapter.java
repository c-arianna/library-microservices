package mentoring.acomi.loanservice.infrastructure.persistence.repositories.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.mapper.LoanEventJpaMapper;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.LoanEventJpaRepository;

@Repository
public class JpaLoanEventRepositoryAdapter implements LoanEventRepository {

	private final LoanEventJpaRepository repository;
	private final LoanEventJpaMapper mapper;

	public JpaLoanEventRepositoryAdapter(LoanEventJpaRepository repository, LoanEventJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void appendToStream(LoanEvent event) {

		Optional<Integer> version = repository.findLastVersion(event.aggregateId());

		Integer nextVersion = version.isEmpty() ? 0 : version.get() + 1;

		LoanEventEntity entity = mapper.toEntity(event);
		entity.setEventVersion(nextVersion);

		repository.save(entity);
	}

	@Override
	public List<LoanEvent> loadStream(String aggregateId) {
		return repository.findEventsForAggregate(aggregateId).stream().map(mapper::toDomain).toList();
	}

	@Override
	public boolean exists(String aggregateId) {
		return repository.existsByAggregateId(aggregateId);
	}

	@Override
	public List<LoanEvent> loadAll() {
		return repository.findAll(Sort.by("eventVersion")).stream().map(mapper::toDomain).toList();
	}

	@Override
	public Optional<LoanEvent> getEvent(String eventType, String aggregateId) {
		Optional<LoanEventEntity> event = repository.getByEventTypeAndAggregateId(eventType, aggregateId);

		return event.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(event.get()));
	}

}
