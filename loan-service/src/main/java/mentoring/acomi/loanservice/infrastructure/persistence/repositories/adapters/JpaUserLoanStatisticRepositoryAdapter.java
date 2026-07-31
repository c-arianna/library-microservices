package mentoring.acomi.loanservice.infrastructure.persistence.repositories.adapters;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.dto.OverdueStatisticDto;
import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticRepository;
import mentoring.acomi.loanservice.application.view.UserLoanStatisticView;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.UserLoanStatisticEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.mapper.UserLoanStatisticJpaMapper;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.UserLoanStatisticJpaRepository;

@Primary
@Repository
public class JpaUserLoanStatisticRepositoryAdapter implements UserLoanStatisticRepository, UserLoanStatisticQueryRepository {
	
	private final UserLoanStatisticJpaRepository repository;
	private final UserLoanStatisticJpaMapper mapper;
	
	public JpaUserLoanStatisticRepositoryAdapter(UserLoanStatisticJpaRepository repository, UserLoanStatisticJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void insert(UserLoanStatisticView view) {
		UserLoanStatisticEntity entity = mapper.toEntity(view);
		repository.save(entity);
	}

	@Override
	public Optional<UserLoanStatisticView> getUserLoanStatistic(String userId) {
		Optional<UserLoanStatisticEntity> entity = repository.getUserLoanStatistic(userId);
		return entity.isEmpty() ? Optional.empty() :  Optional.of(mapper.toDomain(entity.get()));
	}

	@Override
	public void statisticUpdate(String userId, long totalDaysOverdue, LocalDate lastOverdueDate) {
		repository.updateStatistics(userId, totalDaysOverdue, lastOverdueDate);		
	}

	@Override
	public List<OverdueStatisticDto> getOverdueStatistics(LocalDate today) {
		return repository.getOverdueStatistics(today);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();		
	}
	
}
