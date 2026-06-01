package mentoring.acomi.sharedlibrary.integration.messaging.book;

public record BookBorrowRejectedIntegrationPayload(String isbn, String loanId, String userId, String reason) implements BookEventIntegrationPayload{}
