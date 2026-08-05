package mentoring.acomi.loanservice.infrastructure.persistence.repositories.adapters;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.mapper.LoanEventJpaMapper;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.LoanEventJpaRepository;
import mentoring.acomi.sharedjpalibrary.eventstore.AbstractJpaEventRepositoryAdapter;

@Repository
@Transactional
public class JpaLoanEventRepositoryAdapter extends AbstractJpaEventRepositoryAdapter<LoanEvent, LoanEventEntity> implements LoanEventRepository {

	public JpaLoanEventRepositoryAdapter(LoanEventJpaRepository repository, LoanEventJpaMapper mapper) {
		super(repository, mapper);
	}

	@Override
	public List<LoanEventEntity> findAllEvents() {
		return repository.findAllByOrderByOccurredAtAscIdAsc();
	}

}
