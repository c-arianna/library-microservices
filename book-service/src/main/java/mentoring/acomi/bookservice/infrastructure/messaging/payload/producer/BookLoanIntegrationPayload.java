package mentoring.acomi.bookservice.infrastructure.messaging.payload.producer;

import jakarta.validation.constraints.NotBlank;

public record BookLoanIntegrationPayload(@NotBlank String isbn, @NotBlank String loanId, @NotBlank String userId) {}
