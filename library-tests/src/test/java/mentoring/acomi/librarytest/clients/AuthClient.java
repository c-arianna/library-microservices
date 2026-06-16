package mentoring.acomi.librarytest.clients;

import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.jayway.jsonpath.JsonPath;

public class AuthClient {

	private final String baseUrl;
	private final String realm;
	private static final String LOGIN_ENDPOINT = "/realms/%s/protocol/openid-connect/token";

	private RestTestClient client;

	public AuthClient(String baseUrl, String realm) {
		this.baseUrl = baseUrl;
		this.realm = realm;
	}

	public String login(String clientId, String username, String password) {

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "password");
		form.add("client_id", clientId);
		form.add("username", username);
		form.add("password", password);

		client = RestTestClient.bindToServer().baseUrl(baseUrl).build();

		var response = client.post().uri(String.format(LOGIN_ENDPOINT, realm)).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form).exchange().expectBody().returnResult();

		String body = new String(response.getResponseBody(), StandardCharsets.UTF_8);

		return JsonPath.read(body, "$.access_token");
	}
}
