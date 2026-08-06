package mentoring.acomi.bookservice.infrastructure.messaging.payload.producer;

import jakarta.validation.constraints.NotBlank;

public record BookRequestApprovedIntegrationPayload(@NotBlank String requestId) {

}
