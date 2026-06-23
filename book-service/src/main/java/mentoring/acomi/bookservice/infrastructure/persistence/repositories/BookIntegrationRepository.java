package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import java.util.Optional;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.sharedlibrary.eventstore.IntegrationEventRepository;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;

public interface BookIntegrationRepository extends IntegrationEventRepository<IntegrationEventEnvelope<?>> {

	Optional<BookEventEntity> findNextEventToProcess(String aggregateId, String aggregateType, int eventVersion);
	
}
