package mentoring.acomi.userservice.infrastructure.dto;

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserDetail(String userId, String email, String name, String lastname, String cardNumber, UserStatus status, UserRole role) {}
