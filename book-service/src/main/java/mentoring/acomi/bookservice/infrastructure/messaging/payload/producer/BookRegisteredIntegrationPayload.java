package mentoring.acomi.bookservice.infrastructure.messaging.payload.producer;

public record BookRegisteredIntegrationPayload(String isbn, String author, String title, String description) {}
