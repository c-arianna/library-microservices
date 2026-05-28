package mentoring.acomi.loanservice.infrastructure.dto;

import java.time.LocalDate;

import mentoring.acomi.loanservice.domain.model.LoanStatus;

public record LoanDto(String id, String isbn, String userId, LoanStatus status, LocalDate start, LocalDate end) {

}
