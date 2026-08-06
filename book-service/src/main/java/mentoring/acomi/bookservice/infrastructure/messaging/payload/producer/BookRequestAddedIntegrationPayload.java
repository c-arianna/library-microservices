package mentoring.acomi.bookservice.infrastructure.messaging.payload.producer;

import jakarta.validation.constraints.NotBlank;

public record BookRequestAddedIntegrationPayload(@NotBlank String requestId, @NotBlank String author, 
		@NotBlank String title, @NotBlank String requesterUserId, String isbn, String notes) {

}
