package mentoring.acomi.assistantservice.explanation;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import mentoring.acomi.assistantservice.application.context.ExplanationContext;
import mentoring.acomi.assistantservice.application.model.RelatedReadBook;
import mentoring.acomi.assistantservice.application.model.SemanticSearchResponse;
import mentoring.acomi.assistantservice.application.repositories.BookViewRepository;
import mentoring.acomi.assistantservice.application.repositories.ReaderHistoryViewRepository;
import mentoring.acomi.assistantservice.application.repositories.UserViewRepository;
import mentoring.acomi.assistantservice.application.services.explanation.RecommendationExplanationContextBuilder;
import mentoring.acomi.assistantservice.application.services.semantic.SemanticSearchService;
import mentoring.acomi.assistantservice.application.view.BookView;
import mentoring.acomi.assistantservice.application.view.ReaderHistoryView;
import mentoring.acomi.assistantservice.application.view.UserView;
import mentoring.acomi.assistantservice.recommendation.RecommendationDataset;
import mentoring.acomi.assistantservice.recommendation.RecommendationDatasetLoader;
import mentoring.acomi.sharedcorelibrary.model.LoanStatus;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
	    classes = {
	    	RecommendationExplanationContextBuilder.class, 
	        RecommendationDatasetLoader.class,
	        ObjectMapper.class
	    }
	)
public class RecommendationExplanationContextBuilderTest {

	@Autowired
	private RecommendationExplanationContextBuilder builder;

	@MockitoBean
	private SemanticSearchService semanticSearchService;

	@MockitoBean
	private UserViewRepository userRepository;

	@MockitoBean
	private ReaderHistoryViewRepository historyRepository;

	@MockitoBean
	private BookViewRepository bookRepository;

	@Autowired
	private RecommendationDatasetLoader datasetLoader;

	@Test
	void shouldBuildRecommendationContext() {

		UserView user = new UserView("user1", "test@test.it", "keycloak-user", UserStatus.ACTIVE);

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

		when(userRepository.findByUserIdentityProviderId("keycloak-user")).thenReturn(Optional.of(user));

		when(historyRepository.findByUserId("user1")).thenReturn(List.of(new ReaderHistoryView("loan1", "user1",
				visconteDimezzato.isbn(), LocalDate.now(), LocalDate.now().plusMonths(30), LoanStatus.CONFIRMED),
				new ReaderHistoryView("loan2", "user1", 						cavaliereInesistente.isbn(), LocalDate.now(), LocalDate.now().plusMonths(30), LoanStatus.CONFIRMED)));

		when(bookRepository.findByIsbn(recommendedBook.isbn())).thenReturn(Optional.of(recommendedBook));
		when(bookRepository.findByIsbn(visconteDimezzato.isbn())).thenReturn(Optional.of(visconteDimezzato));
		when(bookRepository.findByIsbn(cavaliereInesistente.isbn())).thenReturn(Optional.of(cavaliereInesistente));
		
		SemanticSearchResponse semanticSearchForVisconteDimezzato = new SemanticSearchResponse(visconteDimezzato,
				List.of(new RelatedReadBook(recommendedBook, 0.90),  new RelatedReadBook(cavaliereInesistente, 0.85)));

		when(semanticSearchService.findSimilarBookCandidates(visconteDimezzato.isbn())).thenReturn(semanticSearchForVisconteDimezzato);
		
		SemanticSearchResponse semanticSearchForCavaliereInesistente = new SemanticSearchResponse(cavaliereInesistente,
				List.of(new RelatedReadBook(visconteDimezzato, 0.90), new RelatedReadBook(recommendedBook, 0.85)));

		when(semanticSearchService.findSimilarBookCandidates(cavaliereInesistente.isbn())).thenReturn(semanticSearchForCavaliereInesistente);

		ExplanationContext context = builder.buildExplanationContext("keycloak-user", recommendedBook.isbn());

		List<RelatedReadBook> relatedReadBooks = context.relatedReadBooks();
		assertEquals(2, relatedReadBooks.size());
		List<BookView> relatedBookView = relatedReadBooks.stream().map(RelatedReadBook::book).toList();
		assertTrue(relatedBookView.contains(cavaliereInesistente));
		assertTrue(relatedBookView.contains(visconteDimezzato));
	}
}
