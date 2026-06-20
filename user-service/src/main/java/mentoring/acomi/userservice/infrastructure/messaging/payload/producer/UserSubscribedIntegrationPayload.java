package mentoring.acomi.userservice.infrastructure.messaging.payload.producer;

import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;

public record UserSubscribedIntegrationPayload(String userId, String email, String name, String lastname, String userIdentityProviderId, UserStatus status, UserRole role) {}