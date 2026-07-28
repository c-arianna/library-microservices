package mentoring.acomi.bookservice.infrastructure.messaging.notifications.payload;

public record BookSubscriptionRequestedPayload(long subscriptionId, String isbn, String userIdentityId, String phoneNumber,
		String title) {}
