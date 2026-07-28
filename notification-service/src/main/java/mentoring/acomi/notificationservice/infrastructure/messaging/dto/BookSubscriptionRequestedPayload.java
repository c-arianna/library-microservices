package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

public record BookSubscriptionRequestedPayload(long subscriptionId, String isbn, String userIdentityId, String phoneNumber,
		String title) implements EventNotificationPayload{}
