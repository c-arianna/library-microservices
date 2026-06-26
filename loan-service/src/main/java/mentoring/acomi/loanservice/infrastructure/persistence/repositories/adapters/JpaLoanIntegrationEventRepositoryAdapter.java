package mentoring.acomi.loanservice.infrastructure.persistence.repositories.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.mapper.LoanIntegrationEventJpaMapper;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.LoanEventJpaRepository;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.LoanIntegrationRepository;
import mentoring.acomi.sharedlibrary.eventstore.AbstractJpaIntegrationEventRepositoryAdapter;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;

@Repository
@Transactional
public class JpaLoanIntegrationEventRepositoryAdapter
		extends AbstractJpaIntegrationEventRepositoryAdapter<IntegrationEventEnvelope<?>, LoanEventEntity>
		implements LoanIntegrationRepository {

	public JpaLoanIntegrationEventRepositoryAdapter(LoanEventJpaRepository repository,
			LoanIntegrationEventJpaMapper mapper) {
		super(repository, mapper);
	}

	@Override
	public Optional<LoanEventEntity> findNextEventToProcess(String aggregateId, String aggregateType,
			int eventVersion) {
		return repository.findNextEventToProcess(aggregateId, aggregateType, eventVersion);
	}

	@Override
	public List<LoanEventEntity> findEventsToProcess(String aggregateId, List<String> eventTypes) {
		return repository.findByAggregateIdAndProcessedFalseAndFailedFalseAndEventTypeIn(aggregateId, eventTypes);
	}

	@Override
	public void markFailed(String eventId, String aggregateType) {
		repository.markFailed(eventId, aggregateType);
	}

	@Override
	public void incrementRetry(String eventId, String aggregateType) {
		repository.incrementRetry(eventId, aggregateType);
	}
}