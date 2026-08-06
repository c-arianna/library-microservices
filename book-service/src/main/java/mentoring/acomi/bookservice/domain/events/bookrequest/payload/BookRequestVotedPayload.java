package mentoring.acomi.bookservice.domain.events.bookrequest.payload;

public record BookRequestVotedPayload(String requestId, String userId) {}