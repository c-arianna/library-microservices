package mentoring.acomi.userservice.application.projection;

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserSubscriptionData(String userId, String email, String name, String lastname, String userIdentityProviderId,
		String cardNumber, UserStatus status, UserRole role) {}