package mentoring.acomi.assistantservice.readingpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import mentoring.acomi.assistantservice.application.context.ReadingPathContext;
import mentoring.acomi.assistantservice.application.dto.ReadingPathRecommendation;
import mentoring.acomi.assistantservice.application.model.ReadingPathBook;
import mentoring.acomi.assistantservice.application.model.ReadingPathSelection;
import mentoring.acomi.assistantservice.application.services.readingpath.AIReadingPathService;
import mentoring.acomi.assistantservice.application.services.readingpath.ReadingPathContextBuilder;
import mentoring.acomi.assistantservice.application.services.readingpath.ReadingPathService;
import mentoring.acomi.assistantservice.application.view.BookView;
import mentoring.acomi.assistantservice.recommendation.RecommendationDataset;
import mentoring.acomi.assistantservice.recommendation.RecommendationDatasetLoader;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(classes = { ReadingPathService.class, RecommendationDatasetLoader.class, ObjectMapper.class })
public class ReadingPathServiceTest {

    @Autowired
    private ReadingPathService service;

    @MockitoBean
    private ReadingPathContextBuilder builder;

    @MockitoBean
    private AIReadingPathService aiService;

    @Autowired
	private RecommendationDatasetLoader datasetLoader;
    
    @BeforeEach
	void setupSecurityContext() {

	    Jwt jwt = Jwt.withTokenValue("token")
	            .subject("keycloak-user")
	            .header("alg", "none")
	            .build();

	    Authentication authentication = new JwtAuthenticationToken(jwt);

	    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

	    securityContext.setAuthentication(authentication);

	    SecurityContextHolder.setContext(securityContext);
	}
    
    @Test
    void shouldBuildReadingPath() {

    	List<BookView> catalogBooks = datasetLoader.load();

		BookView barone = catalogBooks.stream().filter(b -> b.isbn().equals(RecommendationDataset.BARONE_RAMPANTE))
				.findFirst().orElseThrow(() -> new IllegalStateException(
						String.format("Book %s not found", RecommendationDataset.BARONE_RAMPANTE)));

		BookView visconteDimezzato = catalogBooks.stream()
				.filter(b -> b.isbn().equals(RecommendationDataset.VISCONTE_DIMEZZATO)).findFirst()
				.orElseThrow(() -> new IllegalStateException(
						String.format("Book %s not found", RecommendationDataset.VISCONTE_DIMEZZATO)));

		BookView cavaliereInesistente = catalogBooks.stream()
				.filter(b -> b.isbn().equals(RecommendationDataset.CAVALIERE_INESISTENTE)).findFirst()
				.orElseThrow(() -> new IllegalStateException(
						String.format("Book %s not found", RecommendationDataset.CAVALIERE_INESISTENTE)));
		
		int cavaliereInesistenteId = 0;
		int baroneId = 1;
		
		Map<Integer, BookView> candidates = Map.of(cavaliereInesistenteId, cavaliereInesistente, baroneId, barone);
        ReadingPathContext context = new ReadingPathContext(visconteDimezzato, candidates);

        when(builder.buildReadingPathContext(anyString())).thenReturn(context);

        ReadingPathSelection selection = new ReadingPathSelection(
                        List.of(new ReadingPathBook(cavaliereInesistenteId, 1),
                                new ReadingPathBook(baroneId, 2)));

        when(aiService.buildReadingPath(context)).thenReturn(selection);
        when(aiService.explainReadingPath(anyList())).thenReturn("Percorso consigliato di Calvino");

        ReadingPathRecommendation result = service.getReadingPath(RecommendationDataset.VISCONTE_DIMEZZATO);

        assertEquals(2, result.books().size());
        assertEquals(RecommendationDataset.CAVALIERE_INESISTENTE, result.books().get(0).isbn());
        assertEquals(RecommendationDataset.BARONE_RAMPANTE, result.books().get(1).isbn());
        assertEquals("Percorso consigliato di Calvino", result.explanation());
    
    }
}