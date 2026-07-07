package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import mentoring.acomi.sharedcorelibrary.eventstore.IntegrationEventRepository;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;

public interface BookIntegrationRepository extends IntegrationEventRepository<IntegrationEventEnvelope<?>> {

}
