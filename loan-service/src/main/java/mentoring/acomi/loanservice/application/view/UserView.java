package mentoring.acomi.loanservice.application.view;

import mentoring.acomi.sharedlibrary.model.UserStatus;

public record UserView(String id, String email, UserStatus status) {

}
