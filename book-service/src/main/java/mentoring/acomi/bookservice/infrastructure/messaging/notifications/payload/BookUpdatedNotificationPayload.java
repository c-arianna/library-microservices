package mentoring.acomi.bookservice.infrastructure.messaging.notifications.payload;

public record BookUpdatedNotificationPayload(String isbn, String title, String author, String description, boolean available, int totalCopies,
	    int borrowedCopies, int reservedCopies) {}
