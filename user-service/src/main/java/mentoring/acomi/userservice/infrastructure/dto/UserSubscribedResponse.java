package mentoring.acomi.userservice.infrastructure.dto;

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserSubscribedResponse(String userId, String email, String userIdentityProviderId,  UserRole role, UserStatus status) {}
