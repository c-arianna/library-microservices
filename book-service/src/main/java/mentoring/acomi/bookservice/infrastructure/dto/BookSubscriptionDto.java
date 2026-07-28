package mentoring.acomi.bookservice.infrastructure.dto;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.BookSubscriptionStatus;

public record BookSubscriptionDto(long subscriptionId, String isbn, String userIdentityId, BookSubscriptionStatus status) {}
