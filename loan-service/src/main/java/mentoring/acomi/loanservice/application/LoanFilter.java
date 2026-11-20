package mentoring.acomi.loanservice.application;

import mentoring.acomi.sharedcorelibrary.model.LoanStatus;

public record LoanFilter(String isbn, String userId, String cardNumber, LoanStatus status) {

}
