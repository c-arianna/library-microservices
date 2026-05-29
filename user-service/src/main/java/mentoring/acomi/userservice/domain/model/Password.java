package mentoring.acomi.userservice.domain.model;

import mentoring.acomi.userservice.application.security.PasswordHasher;
import mentoring.acomi.userservice.domain.errors.InvalidPassword;

public final class Password {

	private final String hashedValue;

	private Password(String hashedValue) {
		this.hashedValue = hashedValue;
	}

	public static Password create(String rawPassword, PasswordHasher hasher) {

		if (rawPassword == null || rawPassword.isBlank()) {
			throw InvalidPassword.empty();
		}

		String hash = hasher.hash(rawPassword);

		return new Password(hash);
	} 

	public static Password fromHash(String hash) {
        return new Password(hash);
    }
	
	public boolean matches(String rawPassword, PasswordHasher hasher) {
        return hasher.matches(rawPassword, this.hashedValue);
    }
	
	public String value() {
		return hashedValue;
	}
}