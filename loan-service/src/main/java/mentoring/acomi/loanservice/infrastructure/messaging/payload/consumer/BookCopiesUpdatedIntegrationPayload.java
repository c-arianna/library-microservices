package mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer;

public record BookCopiesUpdatedIntegrationPayload(String isbn, int quantity) {}
