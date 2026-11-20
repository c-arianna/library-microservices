package mentoring.acomi.assistantservice.infrastructure.messaging.notifications.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookUpdatedNotificationPayload(@NotBlank String isbn, @NotBlank String author, @NotBlank String title, 
		@NotNull String description, boolean available, int totalCopies, int borrowedCopies, int reservedCopies) {}
