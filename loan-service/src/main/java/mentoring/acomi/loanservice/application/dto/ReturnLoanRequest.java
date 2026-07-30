package mentoring.acomi.loanservice.application.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record ReturnLoanRequest(@NotNull LocalDate returnAt) {}
