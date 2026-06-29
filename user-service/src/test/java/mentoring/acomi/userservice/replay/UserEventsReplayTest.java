package mentoring.acomi.userservice.replay;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import mentoring.acomi.userservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.userservice.application.services.UserService;
import mentoring.acomi.userservice.config.RabbitMQConfigTest;
import mentoring.acomi.userservice.config.SecurityTestConfig;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.SuspendRequest;
import mentoring.acomi.userservice.infrastructure.dto.UnsubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.UserSubscribedResponse;
import mentoring.acomi.userservice.infrastructure.messaging.replay.UserReplayService;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserViewEntity;
import mentoring.acomi.userservice.infrastructure.persistence.repositories.UserViewJpaRepository;

@SpringBootTest
@Testcontainers
@ActiveProfiles("H2")
@Import({RabbitMQConfigTest.class, SecurityTestConfig.class})
public class UserEventsReplayTest {

	@Container
	static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

	@SuppressWarnings("resource")
	@Container
	static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.3")
			.withRealmImportFile("keycloak/realm-export-test.json");
	
	@DynamicPropertySource
	static void rabbitProps(DynamicPropertyRegistry registry) {
		registry.add("spring.rabbitmq.host", rabbit::getHost);
		registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
		registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
		registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
	}

	@DynamicPropertySource
	static void keycloakProps(DynamicPropertyRegistry registry) {

		keycloak.start();
		
		registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
				() -> keycloak.getAuthServerUrl() + "/realms/library-microservices");
		registry.add("keycloak.base-url", keycloak::getAuthServerUrl);
		registry.add("keycloak.realm", () -> "library-microservices");
		registry.add("keycloak.admin-realm", () -> "library-microservices");
		registry.add("keycloak.admin-client-id", () -> "user-service-admin");
		registry.add("keycloak.admin-client-secret", () -> "test-secret");
	}
	
	@Autowired
	private UserService userService;

	@Autowired
	private UserViewJpaRepository viewRepository;
	
	@Autowired
	private UserViewQueryRepository userViewRepository;

	@Autowired
	private UserReplayService replayService;

	@Test
	void shouldRebuildProjectionsFromEventsReplay() {

		String email = String.format("test.%s@mail.com", UUID.randomUUID().toString());
		SubscribeRequest request = new SubscribeRequest("Arianna", "Comi", email, "12345678");
		
		UserSubscribedResponse user = userService.subscribe(request, "ROLE_READER");
		
		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			userViewRepository.findById(user.userId()).orElseThrow();
		});
		
		setAuthenticatedUser(user.email(), "ADMIN");

		userService.suspend(new SuspendRequest(user.userId(), "policy violation"));

		userService.unsuspend(new SuspendRequest(user.userId(), "reactivation"));

		userService.unsubscribe(new UnsubscribeRequest("Unsubscribed"));

		List<UserViewEntity> expectedUsers = viewRepository.findAll();

		Assertions.assertThat(!expectedUsers.isEmpty());
		
		replayService.rebuild();

		List<UserViewEntity> actualUsers = viewRepository.findAll();

		Assertions.assertThat(actualUsers).usingRecursiveComparison().isEqualTo(expectedUsers);
	}

	private void setAuthenticatedUser(String email, String role) {

		Jwt jwt = Jwt.withTokenValue("test-token").header("alg", "none").claim("email", email)
				.claim("realm_access", Map.of("roles", List.of(role))).build();

		Authentication auth = new JwtAuthenticationToken(jwt,
				List.of(new SimpleGrantedAuthority(String.join("_", "ROLE", role))));

		SecurityContextHolder.getContext().setAuthentication(auth);
	}
}
