package mentoring.acomi.bookservice.infrastructure.messaging.notifications.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookSubscriptionNotifiedPayload(@NotNull Long subscriptionId, @NotBlank String isbn) {}
