package mentoring.acomi.bookservice.domain.events.bookrequest.payload;

public record BookRequestRejectedPayload(String requestId, String reason) {}
