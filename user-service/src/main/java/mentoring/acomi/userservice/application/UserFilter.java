package mentoring.acomi.userservice.application;

import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserFilter(String email, String name, String lastname, String cardNumber, UserStatus status) {}