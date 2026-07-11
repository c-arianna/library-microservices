package mentoring.acomi.loanservice.application.view;

import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserView(String id, String email, String identityProviderId, UserStatus status) {

}
