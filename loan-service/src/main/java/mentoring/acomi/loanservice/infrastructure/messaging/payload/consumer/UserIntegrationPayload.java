package mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer;

import mentoring.acomi.sharedlibrary.model.UserStatus;

public record UserIntegrationPayload(String userId, UserStatus status) {}
