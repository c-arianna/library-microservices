package mentoring.acomi.userservice.infrastructure.messaging.payload.producer;

import mentoring.acomi.sharedlibrary.model.UserStatus;

public record UserIntegrationPayload(String userId, UserStatus status) {}
