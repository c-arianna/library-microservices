package mentoring.acomi.sharedlibrary.integration.messaging.user;

import mentoring.acomi.sharedlibrary.model.UserStatus;

public record UserIntegrationPayload(String userId, UserStatus status) {}
