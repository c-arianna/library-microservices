package mentoring.acomi.userservice.api;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import mentoring.acomi.userservice.config.SecurityTestConfig;
import mentoring.acomi.userservice.infrastructure.dto.AuthResponse;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.SuspendRequest;
import mentoring.acomi.userservice.infrastructure.dto.UnsubscribeRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(SecurityTestConfig.class)
class UserApiIntegrationTest {

	@LocalServerPort
	int port;

	private RestClient client;

	private static final String UNSUSPEND_ENDPOINT = "/unsuspend";

	private static final String SUSPEND_ENDPOINT = "/suspend";

	private static final String ADMIN_1 = "admin-1";

	private static final String LIBRARIAN_1 = "lib-1";

	private static final String USER_1 = "user-1";

	private static final String ADMIN_ROLE = "ADMIN";

	private static final String LIBRARIAN_ROLE = "LIBRARIAN";

	private static final String READER_ROLE = "READER";

	private final static String UNSUBSCRIBE_ENDPOINT = "/unsubscribe";
	
	private static final String TOKEN_VALUE = "test-token";
	
	@MockitoBean
	private JwtDecoder jwtDecoder;

	@BeforeEach
	public void setup() {
		this.client = RestClient.builder().baseUrl(String.format("http://localhost:%d", port)).build();
	}

	@Test
	void shouldCreateUser() {
		generateToken(READER_ROLE);
		SubscribeRequest request = new SubscribeRequest("Arianna", "Comi", "test@gmail.com", "12345678");
		ResponseEntity<AuthResponse> response = client.post().uri("/subscribe")
				.contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE))
				.body(request).retrieve().toEntity(AuthResponse.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

		AuthResponse body = response.getBody();

		Assertions.assertNotNull(response.getBody());

		Assertions.assertNotNull(body.userId());

	}

	@Test
	public void readerCanUnsubscribe() throws Exception {
		String userId = createUser();
		ResponseEntity<String> response = unsubscribeUser(READER_ROLE, userId);
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void librarianCannotUnsubscribe() throws Exception {
		ResponseEntity<String> response = unsubscribeUser(LIBRARIAN_ROLE, LIBRARIAN_1);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	public void adminCannotUnsubscribe() throws Exception {
		ResponseEntity<String> response = unsubscribeUser(ADMIN_ROLE, ADMIN_1);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void readerCannotSuspendUser() throws Exception {
		ResponseEntity<String> response = suspendUser(READER_ROLE, USER_1);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void librarianCannotSuspendUser() throws Exception {
		ResponseEntity<String> response = suspendUser(LIBRARIAN_ROLE, USER_1);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void adminCanSuspendUser() throws Exception {
		String userId = createUser();
		ResponseEntity<String> response = suspendUser(ADMIN_ROLE, userId);
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	void readerCannotUnsuspendUser() throws Exception {
		ResponseEntity<String> response = unsuspendUser(READER_ROLE, USER_1);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void librarianCannotUnsuspendUser() throws Exception {
		ResponseEntity<String> response = unsuspendUser(LIBRARIAN_ROLE, USER_1);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void adminCanUnsuspendUser() throws Exception {
		String userId = createUser();
		suspendUser(userId);
		ResponseEntity<String> response = unsuspendUser(ADMIN_ROLE, userId);
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	private ResponseEntity<String> unsubscribeUser(String role, String userId) {
		generateToken(role);
		UnsubscribeRequest request = new UnsubscribeRequest("Unsubscribe");
		return client.post().uri(UNSUBSCRIBE_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE)).body(request).exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> suspendUser(String role, String userId) {
		generateToken(role);
		SuspendRequest request = new SuspendRequest(userId, "");
		return client.post().uri(SUSPEND_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE)).body(request).exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> unsuspendUser(String role, String userId) {
		generateToken(role);
		SuspendRequest request = new SuspendRequest(userId, "");
		return client.post().uri(UNSUSPEND_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE)).body(request).exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> toEntity(ClientHttpResponse response) throws IOException {
		String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);

		return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(body);
	}

	private void suspendUser(String userId) {
		ResponseEntity<String> response = suspendUser(ADMIN_ROLE, userId);
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

	}

	private String createUser() {
		generateToken(READER_ROLE);
		SubscribeRequest request = new SubscribeRequest("Arianna", "Comi", "test@gmail.com", "12345678");
		ResponseEntity<AuthResponse> response = client.post().uri("/subscribe")
				.contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE))
				.body(request).retrieve().toEntity(AuthResponse.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

		return response.getBody().userId();
	}

	private void generateToken(String role) {

		Jwt jwt = Jwt.withTokenValue(TOKEN_VALUE).header("alg", "none")
				.claim("email", "test@gmail.com")
				.claim("realm_access", Map.of("roles", List.of(role))).build();

		when(jwtDecoder.decode(TOKEN_VALUE)).thenReturn(jwt);
	}

}