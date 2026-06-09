package mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer;

public record BookLoanIntegrationPayload(String isbn, String loanId, String userId) {}
