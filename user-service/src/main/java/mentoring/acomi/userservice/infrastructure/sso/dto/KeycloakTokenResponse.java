package mentoring.acomi.userservice.infrastructure.sso.dto;

public record KeycloakTokenResponse(String access_token, int expires_in, String token_type) {}
