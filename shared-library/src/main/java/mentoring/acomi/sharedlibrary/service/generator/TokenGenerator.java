package mentoring.acomi.sharedlibrary.service.generator;

import mentoring.acomi.sharedlibrary.model.UserRole;

public interface TokenGenerator {
	public String generateAccessToken(String userId, String email, UserRole role);
}

