package mentoring.acomi.bookservice.domain.events.payload;

public record BookCopiesAddedPayload(String isbn, int quantity) {}
