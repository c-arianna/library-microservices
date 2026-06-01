package mentoring.acomi.sharedlibrary.integration.messaging.book;

public record BookRegisteredIntegrationPayload(String isbn, String author, String title, String description) implements BookEventIntegrationPayload {}
