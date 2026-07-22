package mentoring.acomi.loanservice.infrastructure.dto;

import java.time.LocalDate;

import mentoring.acomi.loanservice.domain.model.LoanStatus;

public record LoanDetailDto(String id, String isbn, LoanStatus status, LocalDate start, LocalDate end, LoanUserDto user) {}
