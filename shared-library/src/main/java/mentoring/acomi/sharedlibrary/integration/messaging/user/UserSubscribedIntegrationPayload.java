package mentoring.acomi.sharedlibrary.integration.messaging.user;

import mentoring.acomi.sharedlibrary.model.UserStatus;

public record UserSubscribedIntegrationPayload(String userId, String email, UserStatus status) {}