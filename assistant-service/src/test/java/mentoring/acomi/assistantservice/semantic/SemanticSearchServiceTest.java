package mentoring.acomi.assistantservice.semantic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import mentoring.acomi.assistantservice.application.model.RelatedReadBook;
import mentoring.acomi.assistantservice.application.model.SemanticSearchResponse;
import mentoring.acomi.assistantservice.application.projection.BookEmbeddingProjection;
import mentoring.acomi.assistantservice.application.repositories.BookViewRepository;
import mentoring.acomi.assistantservice.application.services.semantic.SemanticSearchService;
import mentoring.acomi.assistantservice.recommendation.RecommendationDataset;
import mentoring.acomi.assistantservice.recommendation.RecommendationDatasetLoader;

@SpringBootTest
@Testcontainers
@Tag("semantic")
public class SemanticSearchServiceTest {

	private static final AtomicBoolean initialized = new AtomicBoolean(false);
	
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
		
		registry.add("spring.datasource.url", () -> mysql.getJdbcUrl()+ (mysql.getJdbcUrl().contains("?") ? "&" : "?") + "connectionTimeZone=UTC");
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
	
	}
	
	@Autowired
	private SemanticSearchService service;
	
	@Autowired
	private RecommendationDatasetLoader datasetLoader;
	
	@Autowired
	private BookViewRepository bookRepository;
	
	@Autowired
    private BookEmbeddingProjection bookEmbeddingProjection;
	
	@BeforeEach
	void setup() {

		if (initialized.compareAndSet(false, true)) {
	     
		    datasetLoader.load()
		            .forEach(book -> {
		                bookRepository.upsert(book, Instant.now());
		                bookEmbeddingProjection.index(book);
		            });
		}
	}
	
	@ParameterizedTest
	@MethodSource("similarBooksDataset")
	void shouldFindSimilarBooks(String sourceIsbn, Set<String> expectedBooks) {

		SemanticSearchResponse semanticSearchResponse = service.findSimilarBookCandidates(sourceIsbn);
		List<RelatedReadBook> candidates = semanticSearchResponse.candidates();
		
		Set<String> found = candidates.stream().map(c -> c.book().isbn()).collect(Collectors.toSet());

		assertFalse("No candidates found", candidates.isEmpty());

		expectedBooks.forEach(isbn -> assertTrue(String.format( "Expected related book %s not found for source %s", isbn, sourceIsbn), 
				found.contains(isbn)));
		
		assertFalse("Source book returned in results", found.contains(sourceIsbn));
	}

	static Stream<Arguments> similarBooksDataset() {

		return Stream.of(

				Arguments.of(RecommendationDataset.ASTERIX_1,
						Set.of(RecommendationDataset.ASTERIX_2, RecommendationDataset.ASTERIX_3,
								RecommendationDataset.ASTERIX_4)),

				Arguments.of(RecommendationDataset.UNICORN_PROJECT,
						Set.of(RecommendationDataset.PHOENIX_PROJECT, RecommendationDataset.VIBE_CODING)),

				Arguments.of(RecommendationDataset.VIBE_CODING,
						Set.of(RecommendationDataset.PHOENIX_PROJECT, RecommendationDataset.UNICORN_PROJECT)),

				Arguments.of(RecommendationDataset.BARONE_RAMPANTE,
						Set.of(RecommendationDataset.VISCONTE_DIMEZZATO, RecommendationDataset.CAVALIERE_INESISTENTE)),

				Arguments.of(RecommendationDataset.VISCONTE_DIMEZZATO,
						Set.of(RecommendationDataset.BARONE_RAMPANTE, RecommendationDataset.CAVALIERE_INESISTENTE)),

				Arguments.of(RecommendationDataset.HARRY_POTTER_1,
						Set.of(RecommendationDataset.HARRY_POTTER_6)),

				Arguments.of(RecommendationDataset.HARRY_POTTER_6, Set.of(RecommendationDataset.HARRY_POTTER_1)));
	}
}
