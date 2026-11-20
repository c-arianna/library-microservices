package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserUpdatedNotificationPayload(@NotBlank String userId, @NotBlank String email, @NotBlank String name, 
		@NotBlank String lastname, @NotBlank String userIdentityProviderId, String cardNumber, 
		@NotNull UserStatus status, @NotNull UserRole role) implements EventNotificationPayload {}
