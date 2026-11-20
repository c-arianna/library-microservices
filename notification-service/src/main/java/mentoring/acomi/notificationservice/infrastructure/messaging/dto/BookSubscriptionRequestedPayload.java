package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

import jakarta.validation.constraints.NotBlank;

public record BookSubscriptionRequestedPayload(long subscriptionId, @NotBlank String isbn, @NotBlank String userIdentityId, 
		@NotBlank String phoneNumber, @NotBlank String title) implements EventNotificationPayload{}
