package mentoring.acomi.userservice.application.view;

import java.time.LocalDateTime;

public record RefreshToken(String userId, String token, LocalDateTime expiresAt, boolean revoked) {}
