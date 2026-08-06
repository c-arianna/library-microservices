package mentoring.acomi.bookservice.domain.events.book.payload;

public record BookCopiesAddedPayload(String isbn, int quantity) {}
