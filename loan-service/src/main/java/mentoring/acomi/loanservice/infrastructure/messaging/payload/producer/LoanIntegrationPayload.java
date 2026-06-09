package mentoring.acomi.loanservice.infrastructure.messaging.payload.producer;

public record LoanIntegrationPayload(String loanId, String isbn, String userId) {}