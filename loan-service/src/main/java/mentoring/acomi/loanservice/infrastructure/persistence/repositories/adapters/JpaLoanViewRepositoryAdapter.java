package mentoring.acomi.loanservice.infrastructure.persistence.repositories.adapters;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.LoanFilter;
import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanViewEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.mapper.LoanViewJpaMapper;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.LoanViewJpaRepository;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.spec.JpaLoanViewSpecification;

@Primary
@Repository
public class JpaLoanViewRepositoryAdapter implements LoanViewRepository, LoanViewQueryRepository {

	private final LoanViewJpaRepository repository;
	private final LoanViewJpaMapper mapper;
	
	public JpaLoanViewRepositoryAdapter(LoanViewJpaRepository repository, LoanViewJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}
	
	@Override
	public void insertRequest(LoanView loan, Instant createdAt) {
		LoanViewEntity entity = mapper.toEntity(loan);
		entity.markCreated(createdAt);
		repository.save(entity);	
	}

	@Override
	public void updateStatus(String id, LoanStatus status, Instant updatedAt) {
		repository.updateStatus(id, status, updatedAt);
	}

	@Override
	public Optional<LoanView> findById(String id) {
		Optional<LoanViewEntity> entity = repository.findById(id);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toView(entity.get()));
	}

	@Override
	public List<LoanView> find(LoanFilter filter) {
		Specification<LoanViewEntity> spec = JpaLoanViewSpecification.fromFilter(filter);
		return repository.findAll(spec).stream().map(mapper::toView).toList();
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();	
	}

}
