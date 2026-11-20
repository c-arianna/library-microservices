package mentoring.acomi.loanservice.application.dto;

import java.time.LocalDate;

import mentoring.acomi.sharedcorelibrary.model.LoanStatus;

public record LoanDto(String id, String isbn, String author, String title, String userId, String cardNumber, LocalDate endDate, LoanStatus status, boolean overdue) {}