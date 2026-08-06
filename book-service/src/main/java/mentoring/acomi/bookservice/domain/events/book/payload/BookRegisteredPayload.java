package mentoring.acomi.bookservice.domain.events.book.payload;

public record BookRegisteredPayload(String isbn, String author, String title, String description) {
}
