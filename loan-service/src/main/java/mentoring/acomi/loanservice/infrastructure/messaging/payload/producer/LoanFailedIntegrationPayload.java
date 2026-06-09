package mentoring.acomi.loanservice.infrastructure.messaging.payload.producer;

public record LoanFailedIntegrationPayload(String loanId, String reason) {

}
