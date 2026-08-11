package mentoring.acomi.userservice.application.dto;

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserSubscribedResponse(String userId, String email, String userIdentityProviderId, String cardNumber, UserRole role, 
		UserStatus status) {}
