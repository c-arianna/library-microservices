package mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer;

import jakarta.validation.constraints.NotBlank;

public record LibraryCardAssignedIntegrationPayload(@NotBlank String userId, @NotBlank String cardNumber) {}
