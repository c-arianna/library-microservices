package mentoring.acomi.userservice.infrastructure.dto;

import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;

public record UserResponse(String userId, String email, String userIdentityProviderId, UserRole role, UserStatus status) {

}
