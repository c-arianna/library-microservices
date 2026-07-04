package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

public record BookRegisteredNotificationPayload(String isbn, String author, String title, String description) implements EventNotificationPayload{}
