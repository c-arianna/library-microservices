package mentoring.acomi.apigateway.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiGatewaySecurityTest {

	@LocalServerPort
	private int port;

	private RestClient client;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		this.client = RestClient.builder().baseUrl(String.format("http://localhost:%d", port)).build();
	}

	@Test
	public void shouldAllowAccessToAuthLoginWithoutToken() throws Exception {

		String loginJson = loginPayload("test@mail.com", "Password123!");

		ResponseEntity<String> response = postJson("/auth/login", loginJson);

		Assertions.assertNotEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@Test
	void shouldReturn401WhenNoTokenProvided() throws Exception {
		ResponseEntity<String> response = get("/books");

		Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertErrorCode(response.getBody(), "AUTHENTICATION_ERROR");
	}

	@Test
	void shouldReturn401WhenTokenIsInvalid() throws Exception {
		ResponseEntity<String> response = get("/books", "invalid-token");

		Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertErrorCode(response.getBody(), "AUTHENTICATION_ERROR");
	}

	@Test
	void shouldForwardRequestWhenTokenIsValid() throws Exception {
		String token = obtainValidAccessToken("test@mail.com", "Password123!");

		ResponseEntity<String> response = get("/books", token);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	void shouldReturnConsistentErrorResponseForUnauthorizedRequest() throws Exception {
		ResponseEntity<String> response = get("/books");

		Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

		JsonNode json = readJson(response.getBody());
		Assertions.assertEquals("AUTHENTICATION_ERROR", json.path("code").asString());
		Assertions.assertFalse(json.path("message").asString().isBlank());
		Assertions.assertFalse(json.path("service").asString().isBlank());
		Assertions.assertFalse(json.path("timestamp").asString().isBlank());
	}

	private ResponseEntity<String> postJson(String uri, String jsonBody) {
		return postJson(uri, jsonBody, null);
	}

	private ResponseEntity<String> postJson(String uri, String jsonBody, String bearerToken) {
		return client.post().uri(uri).contentType(MediaType.APPLICATION_JSON).headers(headers -> {
			if (bearerToken != null && !bearerToken.isBlank()) {
				headers.setBearerAuth(bearerToken);
			}
		}).body(jsonBody).exchange((request, response) -> toEntity(response));
	}

	private ResponseEntity<String> toEntity(ClientHttpResponse response) throws IOException {
		String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);

		return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(body);
	}

	private String loginPayload(String email, String password) {
		return """
				{
				  "email": "%s",
				  "password": "%s"
				}
				""".formatted(email, password);
	}

	private ResponseEntity<String> get(String uri) {
		return get(uri, null);
	}

	private ResponseEntity<String> get(String uri, String bearerToken) {
		return client.get().uri(uri).headers(headers -> {
			if (bearerToken != null && !bearerToken.isBlank()) {
				headers.setBearerAuth(bearerToken);
			}
		}).exchange((request, response) -> toEntity(response));
	}

	private void assertErrorCode(String body, String expectedCode) throws Exception {
		JsonNode json = readJson(body);
		Assertions.assertEquals(expectedCode, json.path("code").asString());
	}

	private JsonNode readJson(String body) throws Exception {
		return objectMapper.readTree(body);
	}

	private String obtainValidAccessToken(String email, String password) throws Exception {

		ResponseEntity<String> response = postJson("/auth/login", loginPayload(email, password));

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "Login failed");

		JsonNode json = readJson(response.getBody());
		String accessToken = json.path("accessToken").asString();

		Assertions.assertFalse(accessToken.isBlank(), "Access token not found in login response");
		return accessToken;
	}

}
