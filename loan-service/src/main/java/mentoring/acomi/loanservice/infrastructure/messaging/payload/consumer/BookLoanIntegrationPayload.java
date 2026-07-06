package mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer;

import jakarta.validation.constraints.NotBlank;

public record BookLoanIntegrationPayload(@NotBlank String isbn, @NotBlank String loanId, @NotBlank String userId) {}