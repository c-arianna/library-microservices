package mentoring.acomi.loanservice.application.view;

import java.time.LocalDate;

public record UserLoanStatisticView(String userId, int overdueLoansCount, long totalDaysOverdue, LocalDate lastOverdueDate) {}
