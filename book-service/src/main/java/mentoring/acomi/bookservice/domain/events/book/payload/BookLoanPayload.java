package mentoring.acomi.bookservice.domain.events.book.payload;

public record BookLoanPayload(String isbn, String loanId, String userId) {

}
