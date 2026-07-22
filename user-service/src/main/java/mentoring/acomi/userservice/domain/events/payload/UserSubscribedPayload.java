package mentoring.acomi.userservice.domain.events.payload;

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserSubscribedPayload(String id, String email, String name, String lastname, String userIdentityProviderId, 
		String cardNumber, UserStatus status, UserRole role) {}