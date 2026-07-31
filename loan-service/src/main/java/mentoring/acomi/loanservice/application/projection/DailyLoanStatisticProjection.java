package mentoring.acomi.loanservice.application.projection;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.repositories.DailyLoanStatisticRepository;

@Component
public class DailyLoanStatisticProjection implements DailyLoanStatisticProjectionOperations {

	private final DailyLoanStatisticRepository repository;
	
	public DailyLoanStatisticProjection(DailyLoanStatisticRepository repository) {
		this.repository = repository;
	}

	@Override
	public void registerLoanCreated(LocalDate statisticDate) {
		repository.registerLoanCreated(statisticDate);
	}

	@Override
	public void registerLoanConfirmed(LocalDate statisticDate) {
		repository.registerLoanConfirmed(statisticDate);
	}

	@Override
	public void registerLoanReturned(LocalDate statisticDate) {
		repository.registerLoanReturned(statisticDate);
	}

	@Override
	public void registerLoanCanceled(LocalDate statisticDate) {
		repository.registerLoanCanceled(statisticDate);
	}
	
}
