package mentoring.acomi.bookservice.infrastructure.messaging.payload.producer;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookRequestPriceUpdatedIntegrationPayload(@NotBlank String requestId, @NotNull BigDecimal estimatedPrice) {}