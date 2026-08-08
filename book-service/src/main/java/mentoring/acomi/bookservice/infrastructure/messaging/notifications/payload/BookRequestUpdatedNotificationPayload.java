package mentoring.acomi.bookservice.infrastructure.messaging.notifications.payload;

import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;

public record BookRequestUpdatedNotificationPayload(String requestId, String author, String title, String isbn, int votes,
        BookRequestStatus status) {}