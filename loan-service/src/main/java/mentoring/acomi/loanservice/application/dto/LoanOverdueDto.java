package mentoring.acomi.loanservice.application.dto;

import java.time.LocalDate;

public record LoanOverdueDto(String loanId, String isbn, String userId, String cardNumber, LocalDate dueDate, long daysOverdue) {}
