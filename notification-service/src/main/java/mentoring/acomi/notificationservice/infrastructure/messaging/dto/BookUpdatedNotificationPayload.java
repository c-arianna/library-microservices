package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

public record BookUpdatedNotificationPayload(String isbn, String author, String title, String description, boolean available, 
		int totalCopies, int borrowedCopies, int reservedCopies) implements EventNotificationPayload{}
