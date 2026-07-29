package mentoring.acomi.loanservice.infrastructure.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record ReturnLoanRequest(@NotNull LocalDate returnAt) {}
