package mentoring.acomi.bookservice.domain.events.bookrequest.payload;

public record BookRequestAddedPayload(String requestId, String author, String title, String requesterUserId, String isbn, String notes) {}
