package mentoring.acomi.loanservice.replay;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.services.LoanService;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.config.RabbitMQConfigTest;
import mentoring.acomi.loanservice.config.SecurityTestConfig;
import mentoring.acomi.loanservice.infrastructure.dto.AddLoanRequest;
import mentoring.acomi.loanservice.infrastructure.dto.LoanResponse;
import mentoring.acomi.loanservice.infrastructure.messaging.replay.LoanReplayService;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanViewEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.LoanViewJpaRepository;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@SpringBootTest
@Testcontainers
@ActiveProfiles("H2")
@Import({ RabbitMQConfigTest.class, SecurityTestConfig.class })
public class LoanEventsReplayTest {

	@Container
	private static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

	@DynamicPropertySource
	static void rabbitProps(DynamicPropertyRegistry registry) {
		registry.add("spring.rabbitmq.host", rabbit::getHost);
		registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
		registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
		registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
	}

	@Autowired
	private LoanService loanService;
	
	@Autowired
	private UserViewRepository userViewRepository;
	
	@Autowired
	private LoanViewQueryRepository loanRepository;
	
	@Autowired
	private LoanViewJpaRepository viewRepository;
	
	@Autowired
	private LoanReplayService replayService;
	
	@Autowired
	private RabbitListenerEndpointRegistry registry;
	
	private static final String USER_ID = "user-1";
	private static final String ISBN = "9788804336327";
	
	private static final String TOKEN_VALUE = "test-token";
	
	@AfterEach
	void stopListeners() {
	    registry.stop();
	}
	
	@Test
	void shouldRebuildProjectionsFromEventsReplay() {
		
		userViewRepository.add(new UserView(USER_ID, String.format("test%s@gmail.com", USER_ID), UserStatus.ACTIVE), Instant.now());
		setAuthenticatedUser(USER_ID, "READER");
		
		String loanId = createLoan();
		
		await().atMost(Duration.ofSeconds(50)).untilAsserted(() -> {
			loanRepository.findById(loanId).orElseThrow();
		});
		
		List<LoanViewEntity> expectedLoans = viewRepository.findAll();

		Assertions.assertThat(!expectedLoans.isEmpty());
		
		replayService.rebuild();

		List<LoanViewEntity> actualLoans = viewRepository.findAll();

		Assertions.assertThat(actualLoans).usingRecursiveComparison().isEqualTo(expectedLoans);
		
		
	}
	
	private String createLoan() {

		LoanResponse response = loanService.addLoan(new AddLoanRequest(ISBN, USER_ID, LocalDate.now(), null));

		Assertions.assertThat(response!=null);
		Assertions.assertThat(response.loanId()!=null);
		return response.loanId();
	}
	
	private void setAuthenticatedUser(String userId, String role) {

		Jwt jwt = Jwt.withTokenValue(TOKEN_VALUE).header("alg", "none")
				.claim("email", String.format("test%s@gmail.com", userId))
				.claim("realm_access", Map.of("roles", List.of(role))).build();

		Authentication auth = new JwtAuthenticationToken(jwt,
				List.of(new SimpleGrantedAuthority(String.join("_", "ROLE", role))));

		SecurityContextHolder.getContext().setAuthentication(auth);
	}
	
	
}
