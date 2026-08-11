package mentoring.acomi.bookservice.application.dto;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.BookSubscriptionStatus;

public record BookSubscriptionDto(long subscriptionId, String isbn, String userIdentityId, BookSubscriptionStatus status) {}
