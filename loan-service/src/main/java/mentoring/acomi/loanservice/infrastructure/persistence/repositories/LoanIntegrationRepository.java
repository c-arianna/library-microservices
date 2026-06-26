package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.sharedlibrary.eventstore.IntegrationEventRepository;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;

public interface LoanIntegrationRepository extends IntegrationEventRepository<IntegrationEventEnvelope<?>> {

	Optional<LoanEventEntity> findNextEventToProcess(String aggregateId, String aggregateType, int eventVersion);
	
	List<LoanEventEntity> findEventsToProcess(String aggregateId, List<String> eventTypes);
	
	void markFailed(String eventId, String aggregateType);
	
	void incrementRetry(String eventId, String aggregateType);
	
}
