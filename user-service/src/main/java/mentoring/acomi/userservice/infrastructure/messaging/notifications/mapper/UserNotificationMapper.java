package mentoring.acomi.userservice.infrastructure.messaging.notifications.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.infrastructure.messaging.notifications.payload.UserUpdatedNotificationPayload;

@Component
public class UserNotificationMapper {

	public UserUpdatedNotificationPayload map(UserView user) {
		return new UserUpdatedNotificationPayload(user.id(), user.email(), user.name(), user.lastname(), user.userIdentityProviderId(), user.status(),
				user.role());
    }
}
