package mentoring.acomi.assistantservice.application.services.recommendation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.context.RecommendationContext;
import mentoring.acomi.assistantservice.application.errors.BookNotFound;
import mentoring.acomi.assistantservice.application.errors.UserNotFound;
import mentoring.acomi.assistantservice.application.model.RelatedReadBook;
import mentoring.acomi.assistantservice.application.model.SemanticSearchResponse;
import mentoring.acomi.assistantservice.application.repositories.BookViewRepository;
import mentoring.acomi.assistantservice.application.repositories.ReaderHistoryViewRepository;
import mentoring.acomi.assistantservice.application.repositories.UserViewRepository;
import mentoring.acomi.assistantservice.application.services.semantic.SemanticSearchService;
import mentoring.acomi.assistantservice.application.view.BookView;
import mentoring.acomi.assistantservice.application.view.ReaderHistoryView;
import mentoring.acomi.assistantservice.application.view.UserView;

@Component
public class RecommendationContextBuilder {

	private final Logger logger = LogManager.getLogger(RecommendationContextBuilder.class);

	private final UserViewRepository userRepository;
	private final ReaderHistoryViewRepository historyRepository;
	private final BookViewRepository bookRepository;
	private final SemanticSearchService service;

	public RecommendationContextBuilder(UserViewRepository userRepository,
			ReaderHistoryViewRepository historyRepository, BookViewRepository bookRepository,
			SemanticSearchService service) {
		this.userRepository = userRepository;
		this.historyRepository = historyRepository;
		this.bookRepository = bookRepository;
		this.service = service;
	}

	public RecommendationContext buildRecommendationContext(String userIdentityProviderId) {

		UserView user = getUser(userIdentityProviderId);

		List<ReaderHistoryView> history = historyRepository.findByUserId(user.userId());

		if (history.isEmpty()) {
			List<BookView> popularBooks = getPopolarBooks();
		    return new RecommendationContext(List.of(), popularBooks);
		}
		
		Set<String> readIsbns = history.stream().map(ReaderHistoryView::isbn).collect(Collectors.toSet());

		List<BookView> readBooks = readIsbns.stream().map(bookRepository::findByIsbn).flatMap(Optional::stream).toList();

		Map<String, BookView> similarCandidates = new HashMap<>();

		for (BookView readBook : readBooks) {

			SemanticSearchResponse semanticSearchResponse = service.findSimilarBookCandidates(readBook.isbn());
			List<RelatedReadBook> similarBooks = semanticSearchResponse.candidates();
			similarBooks.forEach(candidate -> {
				BookView book = bookRepository.findByIsbn(candidate.book().isbn()).orElseThrow(
						() -> new BookNotFound(String.format("Book not found %s", candidate.book().isbn())));
				if (book.available() && !readIsbns.contains(book.isbn())) {
					logger.info("add '{}' to similarCandidates", book.title());
					similarCandidates.put(book.isbn(), book);
				}
			});
		}

		List<BookView> candidates = similarCandidates.values().stream().toList();

		return new RecommendationContext(readBooks, candidates);
	}

	private List<BookView> getPopolarBooks() {
		return bookRepository.findMostPopularBooks(5);
	}

	private UserView getUser(String userIdentityProviderId) {

		Optional<UserView> loggedUser = userRepository.findByUserIdentityProviderId(userIdentityProviderId);

		if (loggedUser.isEmpty()) {
			throw new UserNotFound(String.format("User not found, userIdentityProviderId: %s", userIdentityProviderId));
		}

		return loggedUser.get();
	}

}