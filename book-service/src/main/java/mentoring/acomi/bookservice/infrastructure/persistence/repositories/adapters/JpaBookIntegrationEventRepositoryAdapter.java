package mentoring.acomi.bookservice.infrastructure.persistence.repositories.adapters;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.mapper.BookIntegrationEventJpaMapper;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookEventJpaRepository;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookIntegrationRepository;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedjpalibrary.eventstore.AbstractJpaIntegrationEventRepositoryAdapter;

@Repository
@Transactional
public class JpaBookIntegrationEventRepositoryAdapter extends AbstractJpaIntegrationEventRepositoryAdapter<IntegrationEventEnvelope<?>, BookEventEntity> implements BookIntegrationRepository{

	public JpaBookIntegrationEventRepositoryAdapter(BookEventJpaRepository repository, BookIntegrationEventJpaMapper mapper) {
		 super(repository, mapper);
	}
	

}