package mentoring.acomi.loanservice.application.repositories;

import java.time.LocalDate;
import java.util.Optional;

import mentoring.acomi.loanservice.application.view.UserLoanStatisticView;

public interface UserLoanStatisticRepository {
	void insert(UserLoanStatisticView view);
	void statisticUpdate(String userId, long totalDaysOverdue, LocalDate lastOverdueDate);
	Optional<UserLoanStatisticView> getUserLoanStatistic(String userId);
}
