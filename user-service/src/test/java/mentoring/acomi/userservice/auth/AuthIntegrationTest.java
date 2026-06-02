package mentoring.acomi.userservice.auth;

import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;
import mentoring.acomi.userservice.domain.model.Password;
import mentoring.acomi.userservice.infrastructure.dto.AuthResponse;
import mentoring.acomi.userservice.infrastructure.dto.LoginRequest;
import mentoring.acomi.userservice.infrastructure.dto.LogoutRequest;
import mentoring.acomi.userservice.infrastructure.dto.TokenRefreshRequest;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserViewEntity;
import mentoring.acomi.userservice.infrastructure.persistence.repositories.UserViewJpaRepository;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {

	@LocalServerPort
	int port;

	private RestClient client;

	@Autowired
	private UserViewJpaRepository jpaRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final String USER_EMAIL = "test@mail.com";
	private static final String PASSWORD = "Password123!";

	@BeforeEach
	public void setup() {
		this.client = RestClient.builder().baseUrl(String.format("http://localhost:%d/auth", port)).build();

		jpaRepository.deleteAll();
		createUser(USER_EMAIL, PASSWORD);
	}

	@Test
	public void shouldLoginSuccessfully() throws Exception {

		LoginRequest loginRequest = new LoginRequest(USER_EMAIL, PASSWORD);
		ResponseEntity<AuthResponse> response = client.post().uri("/login").contentType(MediaType.APPLICATION_JSON)
				.body(loginRequest).retrieve().toEntity(AuthResponse.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
		AuthResponse body = response.getBody();

		Assertions.assertNotNull(body.userId());
		Assertions.assertNotNull(body.accessToken());
		Assertions.assertNotNull(body.refreshToken());

	}

	@Test
	public void shouldLoginFailWithWrongPassword() throws Exception {

		LoginRequest loginRequest = new LoginRequest(USER_EMAIL, "12");

		Assertions.assertThrows(HttpClientErrorException.Unauthorized.class, () -> {
			client.post().uri("/login").contentType(MediaType.APPLICATION_JSON).body(loginRequest).retrieve()
					.toEntity(AuthResponse.class);
		});

	}

	@Test
	public void shouldRefreshTokenSuccessfully() throws Exception {

		AuthResponse login = loginUser();

		TokenRefreshRequest request = new TokenRefreshRequest(login.refreshToken());
		ResponseEntity<AuthResponse> response = client.post().uri("/refresh").contentType(MediaType.APPLICATION_JSON)
				.body(request).retrieve().toEntity(AuthResponse.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

		AuthResponse body = response.getBody();
		Assertions.assertNotNull(body.accessToken());
		Assertions.assertNotNull(body.refreshToken());

	}

	@Test
	public void shouldRevokeOldTokenAfterRotation() throws Exception {

		AuthResponse login = loginUser();

		refreshToken(login.refreshToken());

		TokenRefreshRequest request = new TokenRefreshRequest(login.refreshToken());

		Assertions.assertThrows(HttpClientErrorException.Unauthorized.class, () -> {
			client.post().uri("/refresh").contentType(MediaType.APPLICATION_JSON).body(request).retrieve()
					.toEntity(AuthResponse.class);
		});
	}

	@Test
	public void shouldGenerateNewRefreshToken() throws Exception {

		AuthResponse login = loginUser();

		AuthResponse refreshed = refreshToken(login.refreshToken());

		assert !login.refreshToken().equals(refreshed.refreshToken());
	}

	@Test
	public void shouldFailWithInvalidRefreshToken() throws Exception {

		TokenRefreshRequest request = new TokenRefreshRequest("1234");

		Assertions.assertThrows(HttpClientErrorException.Unauthorized.class, () -> {
			client.post().uri("/refresh").contentType(MediaType.APPLICATION_JSON).body(request).retrieve()
					.toEntity(AuthResponse.class);
		});
	}

	@Test
	public void shouldLogoutRevokeRefreshToken() throws Exception {

		AuthResponse login = loginUser();

		LogoutRequest logoutRequest = new LogoutRequest(login.refreshToken());
		ResponseEntity<String> logoutResponse = client.post().uri("/logout").contentType(MediaType.APPLICATION_JSON)
				.body(logoutRequest).retrieve().toEntity(String.class);

		Assertions.assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());

		TokenRefreshRequest request = new TokenRefreshRequest(login.refreshToken());

		Assertions.assertThrows(HttpClientErrorException.Unauthorized.class, () -> {
			client.post().uri("/refresh").contentType(MediaType.APPLICATION_JSON).body(request).retrieve()
					.toEntity(AuthResponse.class);
		});
	}

	private AuthResponse loginUser() throws Exception {

		LoginRequest loginRequest = new LoginRequest(USER_EMAIL, PASSWORD);
		ResponseEntity<AuthResponse> response = client.post().uri("/login").contentType(MediaType.APPLICATION_JSON)
				.body(loginRequest).retrieve().toEntity(AuthResponse.class);

		return response.getBody();
	}

	private AuthResponse refreshToken(String refreshToken) throws Exception {

		TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);
		ResponseEntity<AuthResponse> response = client.post().uri("/refresh").contentType(MediaType.APPLICATION_JSON)
				.body(request).retrieve().toEntity(AuthResponse.class);

		return response.getBody();
	}

	private void createUser(String email, String password) {
		String userId = UUID.randomUUID().toString();
		String hashedPassword = passwordEncoder.encode(password);
		UserViewEntity entity = new UserViewEntity(userId, email, "Arianna", "Comi",
				Password.hashed(hashedPassword).value(), UserStatus.ACTIVE, UserRole.READER);
		jpaRepository.saveAndFlush(entity);
	}
}
