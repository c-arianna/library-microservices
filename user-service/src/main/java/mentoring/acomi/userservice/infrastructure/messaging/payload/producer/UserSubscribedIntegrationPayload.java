package mentoring.acomi.userservice.infrastructure.messaging.payload.producer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserSubscribedIntegrationPayload(@NotBlank String userId, @NotNull @Email String email, @NotBlank String name, 
		@NotBlank String lastname, @NotBlank String userIdentityProviderId, @NotNull UserStatus status, @NotNull UserRole role) {}