package mentoring.acomi.userservice.infrastructure.security;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

import mentoring.acomi.userservice.application.security.RefreshTokenGenerator;

@Component
public class OpaqueRefreshTokenGenerator implements RefreshTokenGenerator {

	private final SecureRandom secureRandom = new SecureRandom();

	@Override
	public String generate() {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

}
