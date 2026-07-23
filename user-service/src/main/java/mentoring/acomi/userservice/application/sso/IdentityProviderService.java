package mentoring.acomi.userservice.application.sso;

import mentoring.acomi.sharedcorelibrary.model.UserRole;

public interface IdentityProviderService {
	ProviderUserCreated createUser(String email, String password, String name, String lastName, UserRole role);
	void disableUser(String identityProviderUserId);
	void deleteUser(String identityProviderUserId);
}
