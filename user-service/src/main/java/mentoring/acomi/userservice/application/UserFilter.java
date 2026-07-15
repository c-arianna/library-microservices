package mentoring.acomi.userservice.application;

import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserFilter(String userId, String email, String userIdentityProviderId, UserStatus status) {}