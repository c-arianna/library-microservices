package mentoring.acomi.bookservice.infrastructure.messaging.payload.producer;

import jakarta.validation.constraints.NotBlank;

public record BookRequestVotedIntegrationPayload(@NotBlank String requestId, @NotBlank String userId) {

}
