package mentoring.acomi.assistantservice.recommendation;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import mentoring.acomi.assistantservice.application.model.RelatedReadBook;
import mentoring.acomi.assistantservice.application.model.SemanticSearchResponse;
import mentoring.acomi.assistantservice.application.context.RecommendationContext;
import mentoring.acomi.assistantservice.application.repositories.BookViewRepository;
import mentoring.acomi.assistantservice.application.repositories.ReaderHistoryViewRepository;
import mentoring.acomi.assistantservice.application.repositories.UserViewRepository;
import mentoring.acomi.assistantservice.application.services.recommendation.RecommendationContextBuilder;
import mentoring.acomi.assistantservice.application.services.semantic.SemanticSearchService;
import mentoring.acomi.assistantservice.application.view.BookView;
import mentoring.acomi.assistantservice.application.view.ReaderHistoryView;
import mentoring.acomi.assistantservice.application.view.UserView;
import mentoring.acomi.sharedcorelibrary.model.LoanStatus;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(classes = { RecommendationContextBuilder.class, RecommendationDatasetLoader.class, ObjectMapper.class })
public class RecommendationContextBuilderTest {

	@Autowired
	private RecommendationContextBuilder builder;

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

		when(userRepository.findByUserIdentityProviderId("keycloak-user")).thenReturn(Optional.of(user));

		when(historyRepository.findByUserId("user1")).thenReturn(List.of(new ReaderHistoryView("loan1", "user1",
				readBook.isbn(), LocalDate.now(), LocalDate.now().plusMonths(30), LoanStatus.CONFIRMED)));

		when(bookRepository.findByIsbn(readBook.isbn())).thenReturn(Optional.of(readBook));
		when(bookRepository.findByIsbn(visconteDimezzato.isbn())).thenReturn(Optional.of(visconteDimezzato));
		when(bookRepository.findByIsbn(cavaliereInesistente.isbn())).thenReturn(Optional.of(cavaliereInesistente));

		SemanticSearchResponse semanticSearchResponse = new SemanticSearchResponse(readBook,
				List.of(new RelatedReadBook(visconteDimezzato, 0.90), new RelatedReadBook(cavaliereInesistente, 0.85)));

		when(semanticSearchService.findSimilarBookCandidates(readBook.isbn())).thenReturn(semanticSearchResponse);

		RecommendationContext context = builder.buildRecommendationContext("keycloak-user");

		assertEquals(1, context.readBooks().size());
		assertEquals(2, context.candidates().size());
	}

	@Test
	void shouldReturnPopularBooksWhenUserHasNoHistory() {

		UserView user = new UserView("user1", "test@test.it", "keycloak-user", UserStatus.ACTIVE);

		when(userRepository.findByUserIdentityProviderId("keycloak-user")).thenReturn(Optional.of(user));

		when(historyRepository.findByUserId(user.userId())).thenReturn(List.of());

		List<BookView> catalogBooks = datasetLoader.load();

		BookView visconteDimezzato = catalogBooks.stream()
				.filter(b -> b.isbn().equals(RecommendationDataset.VISCONTE_DIMEZZATO)).findFirst()
				.orElseThrow(() -> new IllegalStateException(
						String.format("Book %s not found", RecommendationDataset.VISCONTE_DIMEZZATO)));

		BookView harryPotter1 = catalogBooks.stream().filter(b -> b.isbn().equals(RecommendationDataset.HARRY_POTTER_1))
				.findFirst().orElseThrow(() -> new IllegalStateException(
						String.format("Book %s not found", RecommendationDataset.HARRY_POTTER_1)));

		when(bookRepository.findMostPopularBooks(anyInt())).thenReturn(List.of(visconteDimezzato, harryPotter1));

		RecommendationContext context = builder.buildRecommendationContext("keycloak-user");

		assertTrue(context.readBooks().isEmpty());

		assertEquals(2, context.candidates().size());

	}
}
