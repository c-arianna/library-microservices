package mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer;

public record LoanIntegrationPayload(String loanId, String isbn, String userId) {}