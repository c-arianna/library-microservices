package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.sharedlibrary.eventstore.IntegrationEventRepository;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;

public interface BookIntegrationRepository extends IntegrationEventRepository<IntegrationEventEnvelope<?>> {

	Optional<BookEventEntity> findNextEventToProcess(String aggregateId, String aggregateType, int eventVersion);

	List<BookEventEntity> findEventsToProcess(String aggregateId, List<String> eventTypes);

	void markFailed(String eventId, String aggregateType);

	void incrementRetry(String eventId, String aggregateType);

}
