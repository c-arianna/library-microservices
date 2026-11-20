package mentoring.acomi.assistantservice.application.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.errors.InvalidUser;
import mentoring.acomi.assistantservice.application.errors.UserNotFound;

@Component
public class UserIdentityProvider {

    public String getUserIdentityProviderId() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null) {
			throw new InvalidUser("User not logged");
		}

		Jwt jwt = (Jwt) auth.getPrincipal();

		String userIdentityProviderId = jwt.getSubject();
		
		if (userIdentityProviderId == null || userIdentityProviderId.isBlank()) {
			throw new UserNotFound("userIdentityProviderId not present in token");
		}
		
		return userIdentityProviderId;
	}
    
}
