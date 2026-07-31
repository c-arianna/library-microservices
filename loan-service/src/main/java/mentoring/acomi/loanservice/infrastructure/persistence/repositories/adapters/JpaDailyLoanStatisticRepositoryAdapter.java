package mentoring.acomi.loanservice.infrastructure.persistence.repositories.adapters;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.repositories.DailyLoanStatisticQueryRepository;
import mentoring.acomi.loanservice.application.repositories.DailyLoanStatisticRepository;
import mentoring.acomi.loanservice.application.view.DailyLoanStatisticView;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.DailyLoanStatisticEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.mapper.DailyLoanStatisticsJpaMapper;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.DailyLoanStatisticJpaRepository;

@Primary
@Repository
public class JpaDailyLoanStatisticRepositoryAdapter implements DailyLoanStatisticRepository, DailyLoanStatisticQueryRepository {

	private final DailyLoanStatisticJpaRepository repository;
	private final DailyLoanStatisticsJpaMapper mapper;
	
	public JpaDailyLoanStatisticRepositoryAdapter(DailyLoanStatisticJpaRepository repository, DailyLoanStatisticsJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void registerLoanCreated(LocalDate statisticsDate) {
		repository.registerLoanCreated(statisticsDate);
	}

	@Override
	public void registerLoanConfirmed(LocalDate statisticsDate) {
		repository.registerLoanConfirmed(statisticsDate);
		
	}

	@Override
	public void registerLoanReturned(LocalDate statisticsDate) {
		repository.registerLoanReturned(statisticsDate);		
	}

	@Override
	public void registerLoanCanceled(LocalDate statisticsDate) {
		repository.registerLoanCanceled(statisticsDate);		
	}

	@Override
	public Optional<DailyLoanStatisticView> findByStatisticDate(LocalDate statisticsDate) {
		Optional<DailyLoanStatisticEntity> entity = repository.findById(statisticsDate);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(entity.get()));
	}

	@Override
	public List<DailyLoanStatisticView> findStatistics(LocalDate from, LocalDate to) {
		return repository.findStatistics(from, to).stream().map(mapper::toDomain).toList();
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();		
	}
	
}
