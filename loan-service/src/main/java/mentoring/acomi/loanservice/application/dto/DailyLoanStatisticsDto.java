package mentoring.acomi.loanservice.application.dto;

import java.time.LocalDate;

public record DailyLoanStatisticsDto(LocalDate statisticDate, int loansCreated, int loansConfirmed, int loansCanceled, int loansReturned) {}
