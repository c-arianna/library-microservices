package mentoring.acomi.userservice.integration.rabbitmq;

import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.model.UserStatus;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.userservice.application.services.UserService;
import mentoring.acomi.userservice.config.RabbitMQConfigTest;
import mentoring.acomi.userservice.config.SecurityTestConfig;
import mentoring.acomi.userservice.domain.events.UserEventType;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.SuspendRequest;
import mentoring.acomi.userservice.infrastructure.dto.UnsubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.UserSubscribedResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import({RabbitMQConfigTest.class, SecurityTestConfig.class})
class UserRabbitIntegrationTest {

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
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private AmqpAdmin amqpAdmin;

	@Autowired
	private TopicExchange eventsExchange;

	@Autowired
	private UserService userService;

	@Autowired
	private UserEventRepository userEventRepository;

	@Autowired
	private UserViewQueryRepository userViewRepository;

	@Autowired
	private ObjectMapper objectMapper;
	
	private static final String ADMIN_ROLE = "ADMIN";
	
	private static final String TOKEN_VALUE = "test-token";

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldPublishUserSubscribedWhenUserSubscribes() {

		String routingKey = IntegrationEventTypes.USER_SUBSCRIBED.getRoutingKey();
		String tmpQueue = createTmpQueue(routingKey);

		UserSubscribedResponse user = subscribeUser();

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var userView = userViewRepository.findById(user.userId()).orElseThrow();
			Assertions.assertEquals(UserStatus.ACTIVE, userView.status());
		});

		var events = userEventRepository.loadStream(user.userId());
		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == UserEventType.UserSubscribed));

		String body = waitForMessageBody(tmpQueue);

		Assertions.assertNotNull(body);

		JsonNode json = objectMapper.readTree(body);

		Assertions.assertEquals("USER_SUBSCRIBED", json.get("eventType").asString());

		Assertions.assertEquals("user-service", json.get("producer").asString());
		Assertions.assertEquals(user.userId(), json.get("aggregateId").asString());

		JsonNode payload = json.get("payload");
		Assertions.assertNotNull(payload);

		Assertions.assertEquals(user.email(), payload.get("email").asString());
		Assertions.assertEquals("ACTIVE", payload.get("status").asString());
	}

	@Test
	void shouldPublishUserSuspendedWhenAdminSuspendsUser() {

		String tmpQueue = createTmpQueue(IntegrationEventTypes.USER_SUSPENDED.getRoutingKey());

		UserSubscribedResponse user = subscribeUser();

		setAuthenticatedUser(user.email(), ADMIN_ROLE);

		userService.suspend(new SuspendRequest(user.userId(), "policy violation"));

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var userView = userViewRepository.findById(user.userId()).orElseThrow();
			Assertions.assertEquals(UserStatus.SUSPENDED, userView.status());
		});

		var events = userEventRepository.loadStream(user.userId());
		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == UserEventType.UserSuspended));

		String body = waitForMessageBody(tmpQueue);

		Assertions.assertNotNull(body);

		JsonNode json = objectMapper.readTree(body);

		Assertions.assertEquals("USER_SUSPENDED", json.get("eventType").asString());

		Assertions.assertEquals("user-service", json.get("producer").asString());
		Assertions.assertEquals(user.userId(), json.get("aggregateId").asString());

		JsonNode payload = json.get("payload");
		Assertions.assertNotNull(payload);

		Assertions.assertEquals(user.userId(), payload.get("userId").asString());
		Assertions.assertEquals("SUSPENDED", payload.get("status").asString());
		
	}

	@Test
	void shouldPublishUserUnsuspendedWhenAdminUnsuspendsUser() {

		String tmpQueue = createTmpQueue(IntegrationEventTypes.USER_UNSUSPENDED.getRoutingKey());

		UserSubscribedResponse user = subscribeUser();

		setAuthenticatedUser(user.email(), ADMIN_ROLE);

		userService.suspend(new SuspendRequest(user.userId(), "temporary suspension"));

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var userView = userViewRepository.findById(user.userId()).orElseThrow();
			Assertions.assertEquals(UserStatus.SUSPENDED, userView.status());
		});

		userService.unsuspend(new SuspendRequest(user.userId(), "reactivation"));

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var userView = userViewRepository.findById(user.userId()).orElseThrow();
			Assertions.assertEquals(UserStatus.ACTIVE, userView.status());
		});

		var events = userEventRepository.loadStream(user.userId());
		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == UserEventType.UserUnsuspended));

		String body = waitForMessageBody(tmpQueue);

		Assertions.assertNotNull(body);

		JsonNode json = objectMapper.readTree(body);

		Assertions.assertEquals("USER_UNSUSPENDED", json.get("eventType").asString());

		Assertions.assertEquals("user-service", json.get("producer").asString());
		Assertions.assertEquals(user.userId(), json.get("aggregateId").asString());

		JsonNode payload = json.get("payload");
		Assertions.assertNotNull(payload);

		Assertions.assertEquals(user.userId(), payload.get("userId").asString());
		Assertions.assertEquals("ACTIVE", payload.get("status").asString());
	
	}

	@Test
	void shouldPublishUserUnsubscribedWhenReaderUnsubscribes() {

		String tmpQueue = createTmpQueue(IntegrationEventTypes.USER_UNSUBSCRIBED.getRoutingKey());

		UserSubscribedResponse user = subscribeUser();

		setAuthenticatedUser(user.email(), ADMIN_ROLE);

		userService.unsubscribe(new UnsubscribeRequest("Unsubscribed"));

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			var userView = userViewRepository.findById(user.userId()).orElseThrow();
			Assertions.assertEquals(UserStatus.DISABLE, userView.status());
		});

		var events = userEventRepository.loadStream(user.userId());
		Assertions.assertTrue(events.stream().anyMatch(e -> e.type() == UserEventType.UserUnsubscribed));

		String body = waitForMessageBody(tmpQueue);

		Assertions.assertNotNull(body);

		JsonNode json = objectMapper.readTree(body);

		Assertions.assertEquals("USER_UNSUBSCRIBED", json.get("eventType").asString());

		Assertions.assertEquals("user-service", json.get("producer").asString());
		Assertions.assertEquals(user.userId(), json.get("aggregateId").asString());

		JsonNode payload = json.get("payload");
		Assertions.assertNotNull(payload);

		Assertions.assertEquals(user.userId(), payload.get("userId").asString());
		Assertions.assertEquals("DISABLE", payload.get("status").asString());
		
	}
		
	private void setAuthenticatedUser(String email, String role) {
		
		Jwt jwt = Jwt.withTokenValue(TOKEN_VALUE).header("alg", "none").claim("email", email)
				.claim("realm_access", Map.of("roles", List.of(role))).build();
		
		Authentication auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(String.join("_", "ROLE", role))));

		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	private String createTmpQueue(String routingKey) {
		String queueName = String.join(".", "probe", routingKey, UUID.randomUUID().toString());

		amqpAdmin.declareExchange(eventsExchange);

		Queue queue = new Queue(queueName, false, false, false);
		amqpAdmin.declareQueue(queue);

		Binding binding = BindingBuilder.bind(queue).to(eventsExchange).with(routingKey);

		amqpAdmin.declareBinding(binding);

		return queueName;

	}

	private String receiveMessageBody(String queueName) {
		var message = rabbitTemplate.receive(queueName, 3000);
		if (message == null) {
			return null;
		}
		return new String(message.getBody(), StandardCharsets.UTF_8);
	}

	private UserSubscribedResponse subscribeUser() {
		String email = String.format("test.%s@mail.com", UUID.randomUUID().toString());
		SubscribeRequest request = new SubscribeRequest("Arianna", "Comi", email, "12345678");
		
		UserSubscribedResponse response = userService.subscribe(request, "ROLE_READER");
		
		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			userViewRepository.findById(response.userId()).orElseThrow();
		});

		return response;
	}
	
	private String waitForMessageBody(String queueName) {
        final String[] holder = new String[1];

        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(100))
            .until(() -> {
                holder[0] = receiveMessageBody(queueName);
                return holder[0] != null;
            });

        return holder[0];
    }
}
