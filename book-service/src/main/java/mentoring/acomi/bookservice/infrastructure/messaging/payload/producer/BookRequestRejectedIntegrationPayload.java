package mentoring.acomi.bookservice.infrastructure.messaging.payload.producer;

import jakarta.validation.constraints.NotBlank;

public record BookRequestRejectedIntegrationPayload(@NotBlank String requestId, String reason) {

}
