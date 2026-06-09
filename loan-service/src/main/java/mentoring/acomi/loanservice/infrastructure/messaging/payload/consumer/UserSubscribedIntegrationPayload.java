package mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer;

import mentoring.acomi.sharedlibrary.model.UserStatus;

public record UserSubscribedIntegrationPayload(String userId, String email, UserStatus status) {}