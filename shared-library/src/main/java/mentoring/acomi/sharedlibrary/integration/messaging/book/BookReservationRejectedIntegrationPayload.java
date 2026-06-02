package mentoring.acomi.sharedlibrary.integration.messaging.book;

public record BookReservationRejectedIntegrationPayload(String isbn, String loanId, String userId, String reason) {}
