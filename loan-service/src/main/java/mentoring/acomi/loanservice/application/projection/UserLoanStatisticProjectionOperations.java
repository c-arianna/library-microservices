package mentoring.acomi.loanservice.application.projection;

import java.time.LocalDate;

public interface UserLoanStatisticProjectionOperations {
	void registerOverdueLoan(String userId, String loanId, LocalDate returnedAt);
}
