package mentoring.acomi.userservice.infrastructure.sso.client;

import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import mentoring.acomi.userservice.infrastructure.sso.dto.KeycloakTokenResponse;
import mentoring.acomi.userservice.infrastructure.sso.keycloak.KeycloakProperties;

@Service
public class KeycloakAdminTokenService {

    private static final String TOKEN_URL_API = "%s/realms/%s/protocol/openid-connect/token";
	private final RestClient restClient;
    private final KeycloakProperties properties;

    public KeycloakAdminTokenService(RestClient.Builder builder, KeycloakProperties properties) {
        this.restClient = builder.build();
        this.properties = properties;
    }

    public String getAccessToken() {
        KeycloakTokenResponse response = restClient.post().uri(String.format(TOKEN_URL_API, properties.baseUrl(),  properties.adminRealm()))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(getBody())
            .retrieve()
            .body(KeycloakTokenResponse.class);

        if (response == null || response.access_token() == null) {
            throw new IllegalStateException("Unable to obtain admin access token from Keycloak");
        }

        return response.access_token();
    }

	private String getBody(){
		return String.format("grant_type=client_credentials&client_id=%s&client_secret=%s", UriUtils.encode(properties.adminClientId(), StandardCharsets.UTF_8),
					UriUtils.encode(properties.adminClientSecret(), StandardCharsets.UTF_8));
		
	}
}
