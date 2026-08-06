package mentoring.acomi.bookservice.application.view;

import java.time.Instant;

public record BookRequestVoteView(String requestId, String userId, Instant createdAt) {}