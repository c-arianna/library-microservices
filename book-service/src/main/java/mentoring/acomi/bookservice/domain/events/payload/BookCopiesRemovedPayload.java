package mentoring.acomi.bookservice.domain.events.payload;

public record BookCopiesRemovedPayload(String isbn, int quantity, String reason) {}
