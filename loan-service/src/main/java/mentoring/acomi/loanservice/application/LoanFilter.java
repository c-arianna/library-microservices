package mentoring.acomi.loanservice.application;

import mentoring.acomi.loanservice.domain.model.LoanStatus;

public record LoanFilter(String isbn, String userId, LoanStatus status) {

}
