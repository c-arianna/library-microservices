package mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer;

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public record UserSubscribedIntegrationPayload(String userId, String email, String name, String lastname, String userIdentityProviderId, 
		UserStatus status, UserRole role) {}