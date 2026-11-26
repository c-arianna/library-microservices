package mentoring.acomi.assistantservice.semantic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

import mentoring.acomi.assistantservice.application.dto.ReadingPathRecommendation;
import mentoring.acomi.assistantservice.application.model.ReadingPathStep;
import mentoring.acomi.assistantservice.application.projection.BookEmbeddingProjection;
import mentoring.acomi.assistantservice.application.repositories.BookViewRepository;
import mentoring.acomi.assistantservice.application.repositories.UserViewRepository;
import mentoring.acomi.assistantservice.application.services.readingpath.ReadingPathService;
import mentoring.acomi.assistantservice.application.view.UserView;
import mentoring.acomi.assistantservice.recommendation.RecommendationDataset;
import mentoring.acomi.assistantservice.recommendation.RecommendationDatasetLoader;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@SpringBootTest
@Testcontainers
@Tag("semantic")
@ActiveProfiles("semantic")
public class RecommendationReadingSemanticTest {
	
	private static final String USER_ID = "user1";

	private static final AtomicBoolean initialized = new AtomicBoolean(false);

	@Autowired
	private ReadingPathService service;

	@Autowired
	private RecommendationDatasetLoader datasetLoader;

	@Autowired
	private BookViewRepository bookRepository;
	
	@Autowired
	private UserViewRepository userRepository;
	
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
	void shouldBuildReadingPathForAsterix() {

	    ReadingPathRecommendation path = service.getReadingPath(RecommendationDataset.ASTERIX_3);

	    Set<String> isbns = path.books().stream().map(ReadingPathStep::isbn).collect(Collectors.toSet());

	    assertEquals(4, path.books().size());
	    
	    assertEquals(path.books().size(), path.books().stream().map(ReadingPathStep::isbn).distinct().count());
	    
	    assertTrue(path.books().stream().map(ReadingPathStep::order).allMatch(order -> order > 0));
	    
	    List<Integer> orders = path.books().stream().map(ReadingPathStep::order).toList();
	    assertEquals(IntStream.rangeClosed(1, orders.size()).boxed().toList(), orders);
	    
	    assertTrue(isbns.contains(RecommendationDataset.ASTERIX_1));
	    assertTrue(isbns.contains(RecommendationDataset.ASTERIX_2));
	    assertTrue(isbns.contains(RecommendationDataset.ASTERIX_3));
	    assertTrue(isbns.contains(RecommendationDataset.ASTERIX_4));
	    
	    assertNotNull(path.explanation());
	    assertFalse(path.explanation().isBlank());

	    assertNotNull(path.explanation());
	    assertFalse(path.explanation().isBlank());
	
	}
	
	@Test
	void shouldBuildReadingPathForHarryPotter() {

	    ReadingPathRecommendation path = service.getReadingPath(RecommendationDataset.HARRY_POTTER_6);

	    List<ReadingPathStep> books = path.books();
			    
	    assertEquals(path.books().size(), path.books().stream().map(ReadingPathStep::isbn).distinct().count());
	    
	    assertTrue(path.books().stream().map(ReadingPathStep::order).allMatch(order -> order > 0));
	    
	    List<Integer> orders = path.books().stream().map(ReadingPathStep::order).toList();
	    assertEquals(IntStream.rangeClosed(1, orders.size()).boxed().toList(), orders);
	    
	    assertEquals(RecommendationDataset.HARRY_POTTER_1, books.get(0).isbn());
	   	    
	    assertNotNull(path.explanation());
	    assertFalse(path.explanation().isBlank());

	    assertNotNull(path.explanation());
	    assertFalse(path.explanation().isBlank());
	    
	}
	
	@Test
	void shouldBuildReadingPathForCalvino() {

	    ReadingPathRecommendation path = service.getReadingPath(RecommendationDataset.BARONE_RAMPANTE);

	    Set<String> isbns = path.books().stream().map(ReadingPathStep::isbn).collect(Collectors.toSet());

	    assertEquals(3, path.books().size());
	    
	    assertEquals(path.books().size(), path.books().stream().map(ReadingPathStep::isbn).distinct().count());
	    
	    assertTrue(path.books().stream().map(ReadingPathStep::order).allMatch(order -> order > 0));
	    
	    List<Integer> orders = path.books().stream().map(ReadingPathStep::order).toList();
	    assertEquals(IntStream.rangeClosed(1, orders.size()).boxed().toList(), orders);
	    
	    assertTrue(isbns.contains(RecommendationDataset.CAVALIERE_INESISTENTE));
	    assertTrue(isbns.contains(RecommendationDataset.VISCONTE_DIMEZZATO));
	    assertTrue(isbns.contains(RecommendationDataset.BARONE_RAMPANTE));
	    
	    assertNotNull(path.explanation());
	    assertFalse(path.explanation().isBlank());

	    assertNotNull(path.explanation());
	    assertFalse(path.explanation().isBlank());
	}
	
	@Test
	void shouldBuildReadingPathForGeneKim() {

	    ReadingPathRecommendation path = service.getReadingPath(RecommendationDataset.UNICORN_PROJECT);

	    Set<String> isbns = path.books().stream().map(ReadingPathStep::isbn).collect(Collectors.toSet());

	    assertEquals(3, path.books().size());
	    
	    assertEquals(path.books().size(), path.books().stream().map(ReadingPathStep::isbn).distinct().count());
	    
	    assertTrue(path.books().stream().map(ReadingPathStep::order).allMatch(order -> order > 0));
	    
	    List<Integer> orders = path.books().stream().map(ReadingPathStep::order).toList();
	    assertEquals(IntStream.rangeClosed(1, orders.size()).boxed().toList(), orders);
	    
	    assertTrue(isbns.contains(RecommendationDataset.PHOENIX_PROJECT));
	    assertTrue(isbns.contains(RecommendationDataset.VIBE_CODING));
	    assertTrue(isbns.contains(RecommendationDataset.UNICORN_PROJECT));
	    
	    assertNotNull(path.explanation());
	    assertFalse(path.explanation().isBlank());
	}
}
