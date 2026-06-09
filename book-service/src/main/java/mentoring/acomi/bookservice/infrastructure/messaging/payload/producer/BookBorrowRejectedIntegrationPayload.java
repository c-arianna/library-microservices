package mentoring.acomi.bookservice.infrastructure.messaging.payload.producer;

public record BookBorrowRejectedIntegrationPayload(String isbn, String loanId, String userId, String reason) {}
