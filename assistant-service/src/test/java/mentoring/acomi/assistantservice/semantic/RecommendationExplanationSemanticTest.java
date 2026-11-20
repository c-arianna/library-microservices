package mentoring.acomi.assistantservice.semantic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import mentoring.acomi.assistantservice.application.dto.RecommendationExplanation;
import mentoring.acomi.assistantservice.application.projection.BookEmbeddingProjection;
import mentoring.acomi.assistantservice.application.repositories.BookViewRepository;
import mentoring.acomi.assistantservice.application.repositories.ReaderHistoryViewRepository;
import mentoring.acomi.assistantservice.application.repositories.UserViewRepository;
import mentoring.acomi.assistantservice.application.services.explanation.RecommendationExplanationService;
import mentoring.acomi.assistantservice.application.view.ReaderHistoryView;
import mentoring.acomi.assistantservice.application.view.UserView;
import mentoring.acomi.assistantservice.recommendation.RecommendationDataset;
import mentoring.acomi.assistantservice.recommendation.RecommendationDatasetLoader;
import mentoring.acomi.sharedcorelibrary.model.LoanStatus;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@SpringBootTest
@Testcontainers
@Tag("semantic")
@ActiveProfiles("semantic")
public class RecommendationExplanationSemanticTest {

	private static final String USER_ID = "user1";

	private static final AtomicBoolean initialized = new AtomicBoolean(false);

	@Autowired
	private RecommendationExplanationService service;

	@Autowired
	private RecommendationDatasetLoader datasetLoader;

	@Autowired
	private BookViewRepository bookRepository;
	
	@Autowired
	private UserViewRepository userRepository;
	
	@Autowired
	private ReaderHistoryViewRepository readerRepository;

	@Autowired
	private BookEmbeddingProjection bookEmbeddingProjection;

	@SuppressWarnings("resource")
	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("pgvector/pgvector:pg17"))
			.withInitScript("pgvector-init.sql");

	@Container
	static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

	@DynamicPropertySource
	static void configure(DynamicPropertyRegistry registry) {
		registry.add("assistant.vectorstore.url", postgres::getJdbcUrl);
		registry.add("assistant.vectorstore.username", postgres::getUsername);
		registry.add("assistant.vectorstore.password", postgres::getPassword);

		registry.add("spring.datasource.url",
				() -> mysql.getJdbcUrl() + (mysql.getJdbcUrl().contains("?") ? "&" : "?") + "connectionTimeZone=UTC");
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

	}

	@BeforeEach
	void setup() {

		if (initialized.compareAndSet(false, true)) {

			datasetLoader.load().forEach(book -> {
				bookRepository.upsert(book, Instant.now());
				bookEmbeddingProjection.index(book);
			});
		}

		UserView user = new UserView(USER_ID, "test@test.it", "keycloak-user", UserStatus.ACTIVE);
		userRepository.upsert(user, Instant.now());
		
		Jwt jwt = Jwt.withTokenValue("token").subject("keycloak-user").header("alg", "none").build();

		Authentication authentication = new JwtAuthenticationToken(jwt);

		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

		securityContext.setAuthentication(authentication);

		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	void shouldExplainCalvinoRecommendation() {

		ReaderHistoryView readBook1 = new ReaderHistoryView("loan1", USER_ID, RecommendationDataset.CAVALIERE_INESISTENTE, LocalDate.now(), LocalDate.now().plusMonths(30), LoanStatus.CONFIRMED);
		ReaderHistoryView readBook2 = new ReaderHistoryView("loan2", USER_ID, RecommendationDataset.VISCONTE_DIMEZZATO, LocalDate.now(), LocalDate.now().plusMonths(30), LoanStatus.CONFIRMED);
		
		readerRepository.upsert(readBook1, Instant.now());
		readerRepository.upsert(readBook2, Instant.now());
		
		RecommendationExplanation explanation = service.explainBookRecommendation(RecommendationDataset.BARONE_RAMPANTE);

		String response = explanation.explanation().toLowerCase();

		assertTrue(response.contains("calvino"));
		assertTrue(response.contains("visconte") || response.contains("cavaliere"));

	}

	@Test
	void shouldExplainGeneKimRecommendation() {

		ReaderHistoryView readBook1 = new ReaderHistoryView("loan1", USER_ID, RecommendationDataset.VIBE_CODING, LocalDate.now(), LocalDate.now().plusMonths(30), LoanStatus.CONFIRMED);
		ReaderHistoryView readBook2 = new ReaderHistoryView("loan2", USER_ID, RecommendationDataset.UNICORN_PROJECT, LocalDate.now(), LocalDate.now().plusMonths(30), LoanStatus.CONFIRMED);
		
		readerRepository.upsert(readBook1, Instant.now());
		readerRepository.upsert(readBook2, Instant.now());
		
		RecommendationExplanation explanation = service.explainBookRecommendation(RecommendationDataset.PHOENIX_PROJECT);

		String response = explanation.explanation().toLowerCase();

		assertTrue(response.contains("gene kim"));
		
	}

	@Test
	void shouldExplainHarryPotterRecommendation() {

		ReaderHistoryView readBook1 = new ReaderHistoryView("loan1", USER_ID, RecommendationDataset.HARRY_POTTER_1, LocalDate.now(), LocalDate.now().plusMonths(30), LoanStatus.CONFIRMED);
				
		readerRepository.upsert(readBook1, Instant.now());
				
		RecommendationExplanation explanation = service.explainBookRecommendation(RecommendationDataset.HARRY_POTTER_6);

		String response = explanation.explanation().toLowerCase();

		assertTrue(response.contains("harry potter"));
		
	}

	@Test
	void shouldNotMentionUnrelatedBooks() {

		ReaderHistoryView readBook1 = new ReaderHistoryView("loan1", USER_ID, RecommendationDataset.VIBE_CODING, LocalDate.now(), LocalDate.now().plusMonths(30), LoanStatus.CONFIRMED);
		ReaderHistoryView readBook2 = new ReaderHistoryView("loan2", USER_ID, RecommendationDataset.UNICORN_PROJECT, LocalDate.now(), LocalDate.now().plusMonths(30), LoanStatus.CONFIRMED);
		
		readerRepository.upsert(readBook1, Instant.now());
		readerRepository.upsert(readBook2, Instant.now());
		
		RecommendationExplanation explanation = service.explainBookRecommendation(RecommendationDataset.PHOENIX_PROJECT);

		String response = explanation.explanation().toLowerCase();
		
		assertFalse(response.contains("asterix"));

		assertFalse(response.contains("harry potter"));
	}
}