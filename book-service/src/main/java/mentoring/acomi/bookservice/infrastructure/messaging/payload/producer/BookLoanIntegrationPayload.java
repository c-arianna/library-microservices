package mentoring.acomi.bookservice.infrastructure.messaging.payload.producer;

public record BookLoanIntegrationPayload(String isbn, String loanId, String userId) {}
