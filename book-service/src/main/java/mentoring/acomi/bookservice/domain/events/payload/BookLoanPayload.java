package mentoring.acomi.bookservice.domain.events.payload;

public record BookLoanPayload(String isbn, String loanId, String userId) {

}
