package mentoring.acomi.userservice.application.security;

import mentoring.acomi.userservice.domain.model.UserRole;

public interface TokenService {
	String generateAccessToken(String userId, String email, UserRole role);
    String generateRefreshToken(String userId);
    boolean isValid(String token);
    String extractSubject(String token);
}

