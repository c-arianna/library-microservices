package mentoring.acomi.bookservice.domain.events.book.payload;

public record BookCopiesRemovedPayload(String isbn, int quantity, String reason) {}
