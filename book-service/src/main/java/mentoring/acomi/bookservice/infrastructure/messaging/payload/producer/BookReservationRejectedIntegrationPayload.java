package mentoring.acomi.bookservice.infrastructure.messaging.payload.producer;

public record BookReservationRejectedIntegrationPayload(String isbn, String loanId, String userId, String reason) {}
