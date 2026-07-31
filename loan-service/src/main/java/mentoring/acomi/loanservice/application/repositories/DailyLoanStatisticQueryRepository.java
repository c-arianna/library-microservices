package mentoring.acomi.loanservice.application.repositories;

import java.time.LocalDate;
import java.util.List;

import mentoring.acomi.loanservice.application.view.DailyLoanStatisticView;

public interface DailyLoanStatisticQueryRepository {
	List<DailyLoanStatisticView> findStatistics(LocalDate from, LocalDate to);
}
