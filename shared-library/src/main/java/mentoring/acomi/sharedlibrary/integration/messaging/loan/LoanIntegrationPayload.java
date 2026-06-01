package mentoring.acomi.sharedlibrary.integration.messaging.loan;

public record LoanIntegrationPayload(String loanId, String isbn, String userId) implements LoanEventIntegrationPayload{

}
