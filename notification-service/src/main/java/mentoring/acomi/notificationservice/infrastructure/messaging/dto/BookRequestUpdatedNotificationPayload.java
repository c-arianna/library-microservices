package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookRequestUpdatedNotificationPayload(@NotBlank String requestId, @NotBlank String author, @NotBlank String title, 
		@NotBlank String isbn, int votes, @NotNull BookRequestStatus status) implements EventNotificationPayload{}