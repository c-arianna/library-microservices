package mentoring.acomi.bookservice.domain.events.payload;

public record BookRegisteredPayload(String isbn, String author, String title, String description) {
}
