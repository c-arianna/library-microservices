package mentoring.acomi.assistantservice.application.view;

import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserView(String userId, String email, String userIdentityProviderId, UserStatus status) implements ProjectionView<String> {

	 @Override
	 public String id() {
		 return userId();
	 }
}
