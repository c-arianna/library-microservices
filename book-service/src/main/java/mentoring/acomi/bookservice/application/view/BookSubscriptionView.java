package mentoring.acomi.bookservice.application.view;

import java.time.Instant;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.BookSubscriptionStatus;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.SubscriptionType;

public record BookSubscriptionView(Long id, String isbn, String userIdentityId, String phoneNumber, BookSubscriptionStatus status, 
		SubscriptionType type, Instant createdAt, Instant notifiedAt) {}
