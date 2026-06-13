package mentoring.acomi.userservice.application.view;

import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;

public record UserView(String id, String email, String name, String lastname, String userIdentityProviderId, UserStatus status, UserRole role) {}
