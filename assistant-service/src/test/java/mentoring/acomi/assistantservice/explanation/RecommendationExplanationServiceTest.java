package mentoring.acomi.assistantservice.explanation;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

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

import mentoring.acomi.assistantservice.application.context.ExplanationContext;
import mentoring.acomi.assistantservice.application.dto.RecommendationExplanation;
import mentoring.acomi.assistantservice.application.model.RelatedReadBook;
import mentoring.acomi.assistantservice.application.security.UserIdentityProvider;
import mentoring.acomi.assistantservice.application.services.explanation.RecommendationExplanationContextBuilder;
import mentoring.acomi.assistantservice.application.services.explanation.RecommendationExplanationService;
import mentoring.acomi.assistantservice.application.services.recommendation.AIExplanationRecommendationService;
import mentoring.acomi.assistantservice.application.view.BookView;
import mentoring.acomi.assistantservice.recommendation.RecommendationDataset;
import mentoring.acomi.assistantservice.recommendation.RecommendationDatasetLoader;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(classes = { RecommendationExplanationService.class, RecommendationDatasetLoader.class, ObjectMapper.class,
		UserIdentityProvider.class})
public class RecommendationExplanationServiceTest {

	@Autowired
	private RecommendationExplanationService service;

	@MockitoBean
	private RecommendationExplanationContextBuilder builder;
	
	@MockitoBean
	private AIExplanationRecommendationService aiService;

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
	void shouldRecommendBooks() {
		
		List<BookView> catalogBooks = datasetLoader.load();

		BookView recommendedBook = catalogBooks.stream().filter(b -> b.isbn().equals(RecommendationDataset.BARONE_RAMPANTE))
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

		ExplanationContext context = new ExplanationContext(recommendedBook, 
				   List.of(new RelatedReadBook(visconteDimezzato, 0.80), 
						   new RelatedReadBook(cavaliereInesistente, 0.79)));

		when(builder.buildExplanationContext(anyString(), eq(recommendedBook.isbn()))).thenReturn(context);

		when(aiService.explainRecommendation(context)).thenReturn("Il libro appartiene allo stesso autore dei libri letti");

		RecommendationExplanation result = service.explainBookRecommendation(recommendedBook.isbn());

		assertEquals(RecommendationDataset.BARONE_RAMPANTE, result.isbn());
		assertTrue(!result.explanation().isBlank());
	}
}
