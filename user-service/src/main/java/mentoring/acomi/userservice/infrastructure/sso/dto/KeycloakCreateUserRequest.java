package mentoring.acomi.userservice.infrastructure.sso.dto;

import java.util.List;

public record KeycloakCreateUserRequest(String username, String email, String firstName, String lastName, boolean enabled, boolean emailVerified,
	    List<KeycloakCredentialRequest> credentials) {}
