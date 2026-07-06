package mentoring.acomi.loanservice.infrastructure.persistence.repositories.adapters;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.mapper.LoanIntegrationEventJpaMapper;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.LoanEventJpaRepository;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.LoanIntegrationRepository;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedjpalibrary.eventstore.AbstractJpaIntegrationEventRepositoryAdapter;

@Repository
@Transactional
public class JpaLoanIntegrationEventRepositoryAdapter 
                 extends AbstractJpaIntegrationEventRepositoryAdapter<IntegrationEventEnvelope<?>, LoanEventEntity>
		implements LoanIntegrationRepository {

	public JpaLoanIntegrationEventRepositoryAdapter(LoanEventJpaRepository repository, LoanIntegrationEventJpaMapper mapper) {
		super(repository, mapper);
	}

}