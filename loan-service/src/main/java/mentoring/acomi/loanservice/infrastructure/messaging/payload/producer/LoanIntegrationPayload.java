package mentoring.acomi.loanservice.infrastructure.messaging.payload.producer;

import jakarta.validation.constraints.NotBlank;

public record LoanIntegrationPayload(@NotBlank String loanId, @NotBlank String isbn, @NotBlank String userId) {}