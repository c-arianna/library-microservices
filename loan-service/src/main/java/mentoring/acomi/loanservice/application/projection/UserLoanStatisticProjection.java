package mentoring.acomi.loanservice.application.projection;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.application.view.UserLoanStatisticView;

@Component
public class UserLoanStatisticProjection implements UserLoanStatisticProjectionOperations {

	private final UserLoanStatisticRepository statisticRepository;
	private final LoanViewQueryRepository loanRepository;

	public UserLoanStatisticProjection(UserLoanStatisticRepository statisticRepository, LoanViewQueryRepository loanRepository) {
		this.statisticRepository = statisticRepository;
		this.loanRepository = loanRepository;
	}

	@Override
	public void registerOverdueLoan(String userId, String loanId, LocalDate returnedAt) {

		LoanView loanView = loanRepository.findById(loanId).orElseThrow();
		LocalDate dueDate = loanView.end();

		if (!returnedAt.isAfter(dueDate)) {
			return;
		}

		Optional<UserLoanStatisticView> statistic = statisticRepository.getUserLoanStatistic(userId);

		long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnedAt);

		if (statistic.isEmpty()) {
			UserLoanStatisticView view = new UserLoanStatisticView(userId, 1, daysOverdue, returnedAt);
			statisticRepository.insert(view);
		} else {
			statisticRepository.statisticUpdate(userId, daysOverdue, returnedAt);
		}

	}

}
