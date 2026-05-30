package mentoring.acomi.userservice.application.security;

import mentoring.acomi.userservice.domain.model.UserRole;

public interface TokenService {
	public String generateAccessToken(String userId, String email, UserRole role);
    public boolean isValid(String token);
    public String extractSubject(String token);
}

