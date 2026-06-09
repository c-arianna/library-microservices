package mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer;

public record LoanFailedIntegrationPayload(String loanId, String reason) {

}
