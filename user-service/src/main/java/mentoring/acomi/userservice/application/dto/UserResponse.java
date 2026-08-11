package mentoring.acomi.userservice.application.dto;

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserResponse(String userId, String email, String name, String lastname, String cardNumber, UserRole role, UserStatus status) {

}
