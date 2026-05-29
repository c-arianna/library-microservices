package mentoring.acomi.userservice.infrastructure.dto;

import mentoring.acomi.userservice.domain.model.UserRole;
import mentoring.acomi.userservice.domain.model.UserStatus;

public record UserResponse(String userId, String email, UserRole role, UserStatus status) {

}
