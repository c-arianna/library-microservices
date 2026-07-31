package mentoring.acomi.loanservice.application.projection;

import java.time.LocalDate;

public interface DailyLoanStatisticProjectionOperations {
	void registerLoanCreated(LocalDate statisticsDate);
	void registerLoanConfirmed(LocalDate statisticsDate);
	void registerLoanReturned(LocalDate statisticsDate);
	void registerLoanCanceled(LocalDate statisticsDate);
}
