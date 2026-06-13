package mentoring.acomi.userservice.domain.events.payload;

import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;

public record UserSubscribedPayload(String id, String email, String name, String lastname, String userIdentityProviderId, UserStatus status, UserRole role) {}