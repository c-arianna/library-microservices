package mentoring.acomi.userservice.application.dto;

import mentoring.acomi.sharedcorelibrary.model.UserRole;

public record UserRegisteredDto(String name,  String lastname, String email, String password, UserRole role) {}
