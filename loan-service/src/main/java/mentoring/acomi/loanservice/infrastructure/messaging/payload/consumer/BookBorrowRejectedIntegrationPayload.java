package mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer;

public record BookBorrowRejectedIntegrationPayload(String isbn, String loanId, String userId, String reason) {}
