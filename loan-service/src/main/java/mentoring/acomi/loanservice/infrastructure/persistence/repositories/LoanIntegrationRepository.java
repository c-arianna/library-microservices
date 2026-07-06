package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import mentoring.acomi.sharedcorelibrary.eventstore.IntegrationEventRepository;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;

public interface LoanIntegrationRepository extends IntegrationEventRepository<IntegrationEventEnvelope<?>> {
	
}
