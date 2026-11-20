package mentoring.acomi.assistantservice.application.services.explanation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.context.ExplanationContext;
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
public class RecommendationExplanationContextBuilder {
	
    private final UserViewRepository userRepository;
    private final ReaderHistoryViewRepository historyRepository;
    private final BookViewRepository bookRepository;
    private final SemanticSearchService service;

    public RecommendationExplanationContextBuilder(UserViewRepository userRepository, ReaderHistoryViewRepository historyRepository,
            BookViewRepository bookRepository, SemanticSearchService service) {
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.bookRepository = bookRepository;
        this.service = service;
    }
    
public ExplanationContext buildExplanationContext(String userIdentityProviderId, String isbn) {
		
		UserView user = getUser(userIdentityProviderId);

		BookView recommendedBook = bookRepository.findByIsbn(isbn)
				      .orElseThrow(() -> new BookNotFound(String.format("Book not found, isbn %s", isbn)));
        
		List<ReaderHistoryView> history = historyRepository.findByUserId(user.userId());
        List<BookView> readBooks = history.stream().map(ReaderHistoryView::isbn).map(bookRepository::findByIsbn)
        		.flatMap(Optional::stream).toList();
        
        List<RelatedReadBook> relatedBooks = new ArrayList<>();

		for (BookView readBook : readBooks) {

			SemanticSearchResponse response = service.findSimilarBookCandidates(readBook.isbn());

			response.candidates().stream().filter(candidate -> candidate.book().isbn().equals(isbn)).findFirst()
					.ifPresent(candidate -> relatedBooks.add(new RelatedReadBook(readBook, candidate.score())));
		}
    	
		List<RelatedReadBook> mostRelevantReadBooks =
		        relatedBooks.stream().sorted(
		                    Comparator.comparing(
		                        RelatedReadBook::score)
		                        .reversed())
		                .limit(3)
		                .toList();
		
        
		return new ExplanationContext(recommendedBook, mostRelevantReadBooks);
	}

    private UserView getUser(String userIdentityProviderId) {
		
		Optional<UserView> loggedUser = userRepository.findByUserIdentityProviderId(userIdentityProviderId);
		
		if(loggedUser.isEmpty()) {
			throw new UserNotFound(String.format("User not found, userIdentityProviderId: %s", userIdentityProviderId));
		}
		
		return loggedUser.get();
	}

	
}