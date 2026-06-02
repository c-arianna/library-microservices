package mentoring.acomi.sharedlibrary.integration.messaging.book;

public record BookCopiesUpdatedIntegrationPayload(String isbn, int quantity) {}
