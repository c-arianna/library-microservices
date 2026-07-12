package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserUpdatedNotificationPayload(String userId, String email, String name, String lastname, String userIdentityProviderId, UserStatus status,
		UserRole role) implements EventNotificationPayload {}
