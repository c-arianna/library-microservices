package mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserIntegrationPayload(@NotBlank String userId, @NotNull UserStatus status) {}
