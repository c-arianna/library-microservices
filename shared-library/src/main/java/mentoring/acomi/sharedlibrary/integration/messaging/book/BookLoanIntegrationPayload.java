package mentoring.acomi.sharedlibrary.integration.messaging.book;

public record BookLoanIntegrationPayload(String isbn, String loanId, String userId) {}
