package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import java.util.Optional;

import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.sharedlibrary.eventstore.IntegrationEventRepository;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;

public interface LoanIntegrationRepository extends IntegrationEventRepository<IntegrationEventEnvelope<?>> {

	Optional<LoanEventEntity> findNextEventToProcess(String aggregateId, String aggregateType, int eventVersion);
	
}
