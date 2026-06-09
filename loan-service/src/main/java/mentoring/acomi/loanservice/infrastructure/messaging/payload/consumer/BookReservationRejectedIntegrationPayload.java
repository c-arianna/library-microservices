package mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer;

public record BookReservationRejectedIntegrationPayload(String isbn, String loanId, String userId, String reason) {}
