package mentoring.acomi.userservice.api;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.SuspendRequest;
import mentoring.acomi.userservice.infrastructure.dto.UnsubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.UserResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserApiIntegrationTest {

	private static final String USER_EMAIL = "test%s@gmail.com";

	@SuppressWarnings("resource")
	@Container
	static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.3")
			.withRealmImportFile("keycloak/realm-export-test.json");

	@Autowired
	private UserViewRepository userViewRepository;
	
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

	@DynamicPropertySource
	static void keycloakProps(DynamicPropertyRegistry registry) {

		keycloak.start();
		
		registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
				() -> keycloak.getAuthServerUrl() + "/realms/library-microservices/protocol/openid-connect/certs");
		registry.add("keycloak.base-url", keycloak::getAuthServerUrl);
		registry.add("keycloak.realm", () -> "library-microservices");
		registry.add("keycloak.admin-realm", () -> "library-microservices");
		registry.add("keycloak.admin-client-id", () -> "user-service-admin");
		registry.add("keycloak.admin-client-secret", () -> "test-secret");
	}
	
	@BeforeEach
	public void setup() {
		this.client = RestClient.builder().baseUrl(String.format("http://localhost:%d", port)).build();
		createUser(LIBRARIAN_1, UserRole.LIBRARIAN);
		createUser(ADMIN_1, UserRole.ADMIN);
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldCreateUser() {
		SubscribeRequest request = new SubscribeRequest("Arianna", "Comi", "test@gmail.com", "12345678");
		ResponseEntity<UserResponse> response = client.post().uri("/subscribe").contentType(MediaType.APPLICATION_JSON)
				.body(request).retrieve()
				.toEntity(UserResponse.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

		UserResponse body = response.getBody();

		Assertions.assertNotNull(response.getBody());

		Assertions.assertNotNull(body.userId());

	}

	@Test
	public void readerCanUnsubscribe() throws Exception {
		UserResponse user = createUser();
		generateToken(READER_ROLE, user.email());
		ResponseEntity<String> response = unsubscribeUser();
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void librarianCannotUnsubscribe() throws Exception {
		generateToken(LIBRARIAN_ROLE, String.format(USER_EMAIL, LIBRARIAN_1));
		ResponseEntity<String> response = unsubscribeUser();
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	public void adminCannotUnsubscribe() throws Exception {
		createUser();
		generateToken(ADMIN_ROLE, String.format(USER_EMAIL, ADMIN_1));
		ResponseEntity<String> response = unsubscribeUser();
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void readerCannotSuspendUser() throws Exception {
		generateToken(READER_ROLE, String.format(USER_EMAIL, USER_1));
		ResponseEntity<String> response = suspendUser(USER_1);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void librarianCannotSuspendUser() throws Exception {
		generateToken(LIBRARIAN_ROLE, String.format(USER_EMAIL, LIBRARIAN_1));
		ResponseEntity<String> response = suspendUser(USER_1);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void adminCanSuspendUser() throws Exception {
		UserResponse user = createUser();
		generateToken(ADMIN_ROLE, String.format(USER_EMAIL, ADMIN_1));
		ResponseEntity<String> response = suspendUser(user.userId());
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	void readerCannotUnsuspendUser() throws Exception {
		generateToken(READER_ROLE, String.format(USER_EMAIL, USER_1));
		ResponseEntity<String> response = unsuspendUser(USER_1);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void librarianCannotUnsuspendUser() throws Exception {
		generateToken(LIBRARIAN_ROLE, String.format(USER_EMAIL, LIBRARIAN_1));
		ResponseEntity<String> response = unsuspendUser(USER_1);
		Assertions.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void adminCanUnsuspendUser() throws Exception {
		UserResponse user = createUser();
		generateToken(ADMIN_ROLE, String.format(USER_EMAIL, ADMIN_1));
		suspendUser(user.userId());
		ResponseEntity<String> response = unsuspendUser(user.userId());
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	private ResponseEntity<String> unsubscribeUser() {
		UnsubscribeRequest request = new UnsubscribeRequest("Unsubscribe");
		return client.post().uri(UNSUBSCRIBE_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE)).body(request)
				.exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> suspendUser(String userId) {
		SuspendRequest request = new SuspendRequest(userId, "");
		return client.post().uri(SUSPEND_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE)).body(request)
				.exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> unsuspendUser(String userId) {
		SuspendRequest request = new SuspendRequest(userId, "");
		return client.post().uri(UNSUSPEND_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", TOKEN_VALUE)).body(request)
				.exchange((req, res) -> toEntity(res));
	}

	private ResponseEntity<String> toEntity(ClientHttpResponse response) throws IOException {
		String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);

		return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(body);
	}

	private UserResponse createUser() {
		String email = String.format("test.%s@gmail.com", UUID.randomUUID().toString());
		SubscribeRequest request = new SubscribeRequest("Arianna", "Comi", email, "12345678");
		ResponseEntity<UserResponse> response = client.post().uri("/subscribe").contentType(MediaType.APPLICATION_JSON)
				.body(request).retrieve()
				.toEntity(UserResponse.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

		UserResponse user = response.getBody();
		
		userViewRepository.add(new UserView(user.userId(), user.email(), "Arianna",  "Comi", user.userIdentityProviderId(), UserStatus.ACTIVE, user.role()),
				Instant.now());
		
		return user;
	}

	private void generateToken(String role, String email) {

		Jwt jwt = Jwt.withTokenValue(TOKEN_VALUE).header("alg", "none").claim("email", email)
				.claim("realm_access", Map.of("roles", List.of(role))).build();

		when(jwtDecoder.decode(TOKEN_VALUE)).thenReturn(jwt);
	}
	
	private void createUser(String userId, UserRole role) {
		String identityProvider = UUID.randomUUID().toString();
		userViewRepository.add(new UserView(userId, String.format(USER_EMAIL, userId), "Test", "Test", identityProvider, UserStatus.ACTIVE, role), Instant.now());
	}

}