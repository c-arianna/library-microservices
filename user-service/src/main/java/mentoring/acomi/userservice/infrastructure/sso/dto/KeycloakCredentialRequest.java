package mentoring.acomi.userservice.infrastructure.sso.dto;

public record KeycloakCredentialRequest(String type, String value, boolean temporary) {}
