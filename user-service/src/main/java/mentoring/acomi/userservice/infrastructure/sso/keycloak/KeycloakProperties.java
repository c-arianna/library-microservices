package mentoring.acomi.userservice.infrastructure.sso.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "keycloak")
@Validated
public record KeycloakProperties(
		@NotBlank
		String baseUrl,
		
		@NotBlank 
		String adminRealm,
		
		@NotBlank 
		String realm,
		
		@NotBlank 
		String adminClientId,
		
		@NotBlank 
		String adminClientSecret) {

}
