package mentoring.acomi.userservice.infrastructure.messaging.payload.producer;

import mentoring.acomi.sharedlibrary.model.UserStatus;

public record UserSubscribedIntegrationPayload(String userId, String email, UserStatus status) {}