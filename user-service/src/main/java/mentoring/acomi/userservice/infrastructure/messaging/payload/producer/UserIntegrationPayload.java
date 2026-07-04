package mentoring.acomi.userservice.infrastructure.messaging.payload.producer;

import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserIntegrationPayload(String userId, UserStatus status) {}
