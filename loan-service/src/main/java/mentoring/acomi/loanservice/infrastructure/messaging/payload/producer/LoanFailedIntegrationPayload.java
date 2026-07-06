package mentoring.acomi.loanservice.infrastructure.messaging.payload.producer;

import jakarta.validation.constraints.NotBlank;

public record LoanFailedIntegrationPayload(@NotBlank String loanId, String reason) {

}
