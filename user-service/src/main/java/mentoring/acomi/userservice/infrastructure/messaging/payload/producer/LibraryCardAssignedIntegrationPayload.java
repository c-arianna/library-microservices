package mentoring.acomi.userservice.infrastructure.messaging.payload.producer;

import jakarta.validation.constraints.NotBlank;

public record LibraryCardAssignedIntegrationPayload(@NotBlank String userId, @NotBlank String cardNumber) {}