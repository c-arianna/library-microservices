package mentoring.acomi.loanservice.infrastructure.dto;

import mentoring.acomi.loanservice.domain.model.LoanStatus;

public record LoanDto(String id, String isbn, String userId, String cardNumber, LoanStatus status) {}