package mentoring.acomi.userservice.domain.model;

import mentoring.acomi.userservice.domain.errors.InvalidPassword;

public final class Password {

	private final String value;

	private Password(String value) {
		this.value = value;
	}

	public static Password hashed(String hashedPassword) {
		
		if (hashedPassword == null || hashedPassword.isBlank()) {
			throw InvalidPassword.empty();
		}
		
        return new Password(hashedPassword);
    }
	
	public String value() {
		return value;
	}
}