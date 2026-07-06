package mentoring.acomi.bookservice.infrastructure.messaging.payload.producer;

import jakarta.validation.constraints.NotBlank;

public record BookCopiesUpdatedIntegrationPayload(@NotBlank String isbn, int quantity) {}
