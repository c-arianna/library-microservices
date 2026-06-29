package mentoring.acomi.userservice.application.sso;

public interface IdentityProviderService {
	ProviderUserCreated createUser(String email, String password, String name, String lastName, String role);
	void disableUser(String identityProviderUserId);
	void deleteUser(String identityProviderUserId);
}
