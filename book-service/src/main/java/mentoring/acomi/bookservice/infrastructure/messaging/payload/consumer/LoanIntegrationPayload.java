package mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer;

import jakarta.validation.constraints.NotBlank;

public record LoanIntegrationPayload(@NotBlank String loanId, @NotBlank String isbn, @NotBlank String userId) {}