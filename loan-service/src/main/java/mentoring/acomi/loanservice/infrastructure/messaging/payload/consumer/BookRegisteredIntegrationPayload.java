package mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookRegisteredIntegrationPayload(@NotBlank String isbn, @NotBlank String author, @NotBlank String title, 
		@NotNull String description) {}
