package mentoring.acomi.loanservice.application.repositories;

import java.time.LocalDate;
import java.util.Optional;

import mentoring.acomi.loanservice.application.view.DailyLoanStatisticView;

public interface DailyLoanStatisticRepository {
	void registerLoanCreated(LocalDate statisticsDate);
	void registerLoanConfirmed(LocalDate statisticsDate);
	void registerLoanReturned(LocalDate statisticsDate);
	void registerLoanCanceled(LocalDate statisticsDate);
	Optional<DailyLoanStatisticView> findByStatisticDate(LocalDate statisticsDate);
	void deleteAll();
}
