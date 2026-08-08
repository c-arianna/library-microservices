package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

public record BookRequestUpdatedNotificationPayload(String requestId, String author, String title, String isbn, int votes,
        BookRequestStatus status) implements EventNotificationPayload{}