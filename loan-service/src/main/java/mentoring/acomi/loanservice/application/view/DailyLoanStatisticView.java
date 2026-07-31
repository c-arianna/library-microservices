package mentoring.acomi.loanservice.application.view;

import java.time.LocalDate;

public record DailyLoanStatisticView(LocalDate statisticsDate, int loansCreated, int loansConfirmed, int loansCanceled, int loansReturned) {}