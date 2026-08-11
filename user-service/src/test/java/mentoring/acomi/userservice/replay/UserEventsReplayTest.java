package mentoring.acomi.userservice.replay;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.dto.SubscribeRequest;
import mentoring.acomi.userservice.application.dto.SuspendRequest;
import mentoring.acomi.userservice.application.dto.UnsubscribeRequest;
import mentoring.acomi.userservice.application.dto.UserSubscribedResponse;
import mentoring.acomi.userservice.application.outbox.OutboxPublisher;
import mentoring.acomi.userservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.userservice.application.services.UserService;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.config.RabbitMQConfigTest;
import mentoring.acomi.userservice.config.SecurityTestConfig;
import mentoring.acomi.userservice.infrastructure.messaging.replay.UserReplayService;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserViewEntity;
import mentoring.acomi.userservice.infrastructure.persistence.repositories.UserViewJpaRepository;
import mentoring.acomi.userservice.testcontainers.AbstractKeycloakIntegrationTest;

@SpringBootTest(properties = { "spring.jpa.hibernate.ddl-auto=none", "spring.sql.init.mode=always"})
@Testcontainers
@Import({RabbitMQConfigTest.class, SecurityTestConfig.class})
public class UserEventsReplayTest extends AbstractKeycloakIntegrationTest {

	@Container
	static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");
	
	@Container
	private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");
	
	@DynamicPropertySource
	static void rabbitProps(DynamicPropertyRegistry registry) {
		registry.add("spring.rabbitmq.host", rabbit::getHost);
		registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
		registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
		registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
		
		registry.add("spring.datasource.url", () -> mysql.getJdbcUrl()+ (mysql.getJdbcUrl().contains("?") ? "&" : "?") + "connectionTimeZone=UTC");
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
	}
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserViewJpaRepository viewRepository;
	
	@Autowired
	private UserViewQueryRepository userViewRepository;

	@Autowired
	private UserReplayService replayService;

	@Autowired
	private RabbitListenerEndpointRegistry registry;
	
	@Autowired
	private OutboxPublisher outboxPublisher;
	
	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
		registry.stop();
	}
	
	@Test
	void shouldRebuildProjectionsFromEventsReplay() {

		String email = String.format("test.%s@mail.com", UUID.randomUUID().toString());
		SubscribeRequest request = new SubscribeRequest("Arianna", "Comi", email, "12345678");
		
		UserSubscribedResponse user = userService.subscribe(request);
		
		outboxPublisher.publishPendingEvents();
		
		await().atMost(Duration.ofSeconds(6)).untilAsserted(() -> {	
			Optional<UserView> userView = userViewRepository.findById(user.userId());
			Assertions.assertThat(userView).isPresent();
			Assertions.assertThat(userView.get().status()).isEqualTo(UserStatus.ACTIVE);
		});
		
		setAuthenticatedUser(user.email(), "ADMIN");

		userService.suspend(new SuspendRequest(user.userId(), "policy violation"));

		outboxPublisher.publishPendingEvents();
		
		await().atMost(Duration.ofSeconds(6)).untilAsserted(() -> {
		    UserView userView = userViewRepository.findById(user.userId()).orElseThrow();
			Assertions.assertThat(userView.status()).isEqualTo(UserStatus.SUSPENDED);
		});
		
		userService.unsuspend(new SuspendRequest(user.userId(), "reactivation"));

		outboxPublisher.publishPendingEvents();
		
		await().atMost(Duration.ofSeconds(6)).untilAsserted(() -> {	
		    UserView userView = userViewRepository.findById(user.userId()).orElseThrow();
			Assertions.assertThat(userView.status()).isEqualTo(UserStatus.ACTIVE);
		});
		
		userService.unsubscribe(new UnsubscribeRequest("Unsubscribed"));
		outboxPublisher.publishPendingEvents();
		
		await().atMost(Duration.ofSeconds(6)).untilAsserted(() -> {
	           UserViewEntity userView = viewRepository.findById(user.userId()).orElseThrow();
	           Assertions.assertThat(userView.getStatus()).isEqualTo(UserStatus.DISABLED);
	       });
		
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
