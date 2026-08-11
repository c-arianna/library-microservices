package mentoring.acomi.userservice.infrastructure.sso.keycloak;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.userservice.application.errors.IdentityProviderError;
import mentoring.acomi.userservice.application.errors.IdentityProviderException;
import mentoring.acomi.userservice.application.sso.IdentityProviderService;
import mentoring.acomi.userservice.application.sso.ProviderUserCreated;
import mentoring.acomi.userservice.infrastructure.sso.client.KeycloakAdminTokenService;
import mentoring.acomi.userservice.infrastructure.sso.dto.KeycloakCreateUserRequest;
import mentoring.acomi.userservice.infrastructure.sso.dto.KeycloakCredentialRequest;
import mentoring.acomi.userservice.infrastructure.sso.dto.KeycloakRole;
import mentoring.acomi.userservice.infrastructure.sso.keycloak.errors.KeycloakException;

@Service
public class KeycloakIdentityProviderService implements IdentityProviderService {

	private static final String CREATE_USER_ENDPOINT = "%s/admin/realms/%s/users";
	private static final String ASSIGN_ROLE_ENDPOINT = "%s/admin/realms/%s/users/%s/role-mappings/realm";
	private static final String ROLE_INFO_ENDPOINT = "%s/admin/realms/%s/roles/%s";
	private static final String DELETE_USER_ENDPOINT = "%s/admin/realms/%s/users/%s";

	private final RestClient restClient;
	private final KeycloakProperties properties;
	private final KeycloakAdminTokenService tokenService;

	public KeycloakIdentityProviderService(RestClient.Builder builder, KeycloakProperties properties, KeycloakAdminTokenService tokenService) {
		this.restClient = builder.build();
		this.properties = properties;
		this.tokenService = tokenService;
	}

	@Override
	public ProviderUserCreated createUser(String email, String password, String name, String lastName, UserRole role) {

		try {

			boolean temporaryPassword = UserRole.READER != role;
			
			String userRole = "ROLE_%s".formatted(role.toString());
			String userIdentityProviderId = createUser(email, password, name, lastName, temporaryPassword);

			assignRole(userIdentityProviderId, userRole);
			
			return new ProviderUserCreated(userIdentityProviderId, email, email);

		} catch (RestClientResponseException ex) {
			throw mapKeycloakError(ex, email);
		}

	}

	private String createUser(String email, String password, String name, String lastName, boolean temporaryPassword) {
		
		String accessToken = tokenService.getAccessToken();

		KeycloakCredentialRequest credential = new KeycloakCredentialRequest("password", password, temporaryPassword);
		KeycloakCreateUserRequest request = new KeycloakCreateUserRequest(email, email, name, lastName, true, true,
				List.of(credential));

		ResponseEntity<Void> response = restClient.post()
				.uri(String.format(CREATE_USER_ENDPOINT, properties.baseUrl(), properties.realm()))
				.header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", accessToken))
				.contentType(MediaType.APPLICATION_JSON).body(request).retrieve().toBodilessEntity();

		URI location = response.getHeaders().getLocation();

		if (location == null) {
			throw new KeycloakException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Keycloak user created but Location header is missing");
		}

		return extractIdFromLocation(location);
	}

	private IdentityProviderException mapKeycloakError(RestClientResponseException ex, String email) {

		HttpStatusCode status = ex.getStatusCode();

		return switch (status.value()) {
			case 400 -> new IdentityProviderException(IdentityProviderError.INVALID_USER_DATA, String.format("Invalid user data for ", email));
			case 403 -> new IdentityProviderException(IdentityProviderError.AUTHORIZATION_DENIED, "Not authorized to create user in Keycloak");
			case 404 -> new IdentityProviderException( IdentityProviderError.USER_ALREADY_EXISTS, String.format("User not found: ", email));
			case 409 -> new IdentityProviderException(IdentityProviderError.USER_NOT_FOUND, String.format("User already exists: ", email));
			default -> new IdentityProviderException(IdentityProviderError.GENERIC_ERROR, String.format("Keycloak error: ", ex.getResponseBodyAsString()));
		};
	}

	private String extractIdFromLocation(URI location) {
		String path = location.getPath();
		return path.substring(path.lastIndexOf('/') + 1);
	}

	private void assignRole(String userId, String roleName) {

		String accessToken = tokenService.getAccessToken();

		KeycloakRole role = getRealmRole(roleName);

		restClient.post().uri(String.format(ASSIGN_ROLE_ENDPOINT, properties.baseUrl(), properties.realm(), userId))
				.header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", accessToken)).contentType(MediaType.APPLICATION_JSON)
				.body(List.of(role)).retrieve().toBodilessEntity();
	}

	private KeycloakRole getRealmRole(String roleName) {
		
		String accessToken = tokenService.getAccessToken();
		
		return restClient.get().uri(String.format(ROLE_INFO_ENDPOINT, properties.baseUrl(), properties.realm(), roleName))
		.header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", accessToken)).retrieve().body(KeycloakRole.class);
		
	}
	
	@Override
	public void deleteUser(String identityProviderUserId) {
		
		String accessToken = tokenService.getAccessToken();
		
		try {
			restClient.delete().uri(String.format(DELETE_USER_ENDPOINT, properties.baseUrl(), properties.realm(), identityProviderUserId))
			.header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", accessToken))
			.retrieve()
		    .toBodilessEntity();
		}catch (RestClientResponseException ex) {
			if (HttpStatus.NOT_FOUND.equals(ex.getStatusCode())) {
				return;
			}
			throw mapKeycloakError(ex, identityProviderUserId);
		}

	}

}
