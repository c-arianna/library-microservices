package mentoring.acomi.assistantservice.recommendation;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
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

import mentoring.acomi.assistantservice.application.context.RecommendationContext;
import mentoring.acomi.assistantservice.application.dto.BookRecommendation;
import mentoring.acomi.assistantservice.application.dto.BookRecommendationsResult;
import mentoring.acomi.assistantservice.application.model.RecommendationType;
import mentoring.acomi.assistantservice.application.security.UserIdentityProvider;
import mentoring.acomi.assistantservice.application.services.recommendation.RecommendationContextBuilder;
import mentoring.acomi.assistantservice.application.services.recommendation.RecommendationService;
import mentoring.acomi.assistantservice.application.view.BookView;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(classes = { RecommendationService.class, RecommendationDatasetLoader.class, ObjectMapper.class,
		UserIdentityProvider.class })
public class RecommendationServiceTest {

	@Autowired
	private RecommendationService service;

	@MockitoBean
	private RecommendationContextBuilder builder;

	@Autowired
	private RecommendationDatasetLoader datasetLoader;

	@BeforeEach
	void setupSecurityContext() {

		Jwt jwt = Jwt.withTokenValue("token").subject("keycloak-user").header("alg", "none").build();

		Authentication authentication = new JwtAuthenticationToken(jwt);

		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

		securityContext.setAuthentication(authentication);

		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	void shouldRecommendBooks() {

		List<BookView> catalogBooks = datasetLoader.load();

		BookView readBook = catalogBooks.stream().filter(b -> b.isbn().equals(RecommendationDataset.BARONE_RAMPANTE))
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
		
		List<BookView> candidates = List.of(visconteDimezzato, cavaliereInesistente);
		
		RecommendationContext context = new RecommendationContext(List.of(readBook), candidates);
		
		when(builder.buildRecommendationContext(anyString())).thenReturn(context);

		BookRecommendationsResult result = service.recommend();

		assertEquals(RecommendationType.PERSONALIZED, result.type());
		
		List<BookRecommendation> books = result.books();
		assertFalse(books.isEmpty());
		assertEquals(RecommendationDataset.VISCONTE_DIMEZZATO, books.getFirst().isbn());
	}

	@Test
	void shouldRecommendBooksForNewReader() {

		List<BookView> catalogBooks = datasetLoader.load();

		BookView visconteDimezzato = catalogBooks.stream()
				.filter(b -> b.isbn().equals(RecommendationDataset.VISCONTE_DIMEZZATO)).findFirst()
				.orElseThrow(() -> new IllegalStateException(
						String.format("Book %s not found", RecommendationDataset.VISCONTE_DIMEZZATO)));

		BookView harryPotter1 = catalogBooks.stream().filter(b -> b.isbn().equals(RecommendationDataset.HARRY_POTTER_1))
				.findFirst().orElseThrow(() -> new IllegalStateException(
						String.format("Book %s not found", RecommendationDataset.HARRY_POTTER_1)));

		List<BookView> candidates = List.of(visconteDimezzato, harryPotter1);
		RecommendationContext context = new RecommendationContext(List.of(), candidates);

		when(builder.buildRecommendationContext(anyString())).thenReturn(context);

		BookRecommendationsResult result = service.recommend();

		assertEquals(RecommendationType.POPULAR, result.type());
		assertFalse(result.books().isEmpty());
	}
}
