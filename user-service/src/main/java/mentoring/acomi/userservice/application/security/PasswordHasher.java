package mentoring.acomi.userservice.application.security;

public interface PasswordHasher {

	String hash(String rawPassword);
	boolean matches(String rawPassword, String hashedPassword);

}
