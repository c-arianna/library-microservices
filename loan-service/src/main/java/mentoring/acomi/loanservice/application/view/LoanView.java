package mentoring.acomi.loanservice.application.view;

import java.time.LocalDate;

import mentoring.acomi.loanservice.domain.model.LoanStatus;

public record LoanView(String id, String isbn, String userId, LocalDate start, LocalDate end, LoanStatus status, LocalDate returnedAt) {

}
