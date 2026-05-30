package mentoring.acomi.userservice.application.view;

import mentoring.acomi.userservice.domain.model.UserRole;
import mentoring.acomi.userservice.domain.model.UserStatus;

public record UserView(String id, String email, String name, String lastname, String passwordHash, UserStatus status, UserRole role) {}
