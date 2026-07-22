package mentoring.acomi.userservice.infrastructure.messaging.notifications.payload;

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserUpdatedNotificationPayload(String userId, String email, String name, String lastname, String userIdentityProviderId,
		String cardNumber, UserStatus status, UserRole role) {}
