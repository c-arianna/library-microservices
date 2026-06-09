package mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer;

public record BookRegisteredIntegrationPayload(String isbn, String author, String title, String description) {}
