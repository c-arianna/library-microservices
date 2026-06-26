package mentoring.acomi.bookservice.infrastructure.persistence.repositories.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.mapper.BookIntegrationEventJpaMapper;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookEventJpaRepository;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookIntegrationRepository;
import mentoring.acomi.sharedlibrary.eventstore.AbstractJpaIntegrationEventRepositoryAdapter;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;

@Repository
@Transactional
public class JpaBookIntegrationEventRepositoryAdapter extends AbstractJpaIntegrationEventRepositoryAdapter<IntegrationEventEnvelope<?>, BookEventEntity> implements BookIntegrationRepository{

	public JpaBookIntegrationEventRepositoryAdapter(BookEventJpaRepository repository, BookIntegrationEventJpaMapper mapper) {
		 super(repository, mapper);
	}

	@Override
	public Optional<BookEventEntity> findNextEventToProcess(String aggregateId, String aggregateType, int eventVersion){
		return repository.findNextEventToProcess(aggregateId, aggregateType, eventVersion);
	}
	
	@Override
	public List<BookEventEntity> findEventsToProcess(String aggregateId, List<String> eventTypes) {
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