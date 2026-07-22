package mentoring.acomi.userservice.application.view;

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserView(String id, String email, String name, String lastname, String userIdentityProviderId, String cardNumber, 
		UserStatus status, UserRole role) {}
