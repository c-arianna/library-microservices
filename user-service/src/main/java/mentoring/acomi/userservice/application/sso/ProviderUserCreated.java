package mentoring.acomi.userservice.application.sso;

public record ProviderUserCreated(String identityProviderId, String username, String email) {}
