package mentoring.acomi.bookservice.infrastructure.messaging.payload.producer;

public record BookCopiesUpdatedIntegrationPayload(String isbn, int quantity) {}
