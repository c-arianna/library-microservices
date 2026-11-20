package mentoring.acomi.assistantservice.application.services.readingpath;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.context.ReadingPathContext;
import mentoring.acomi.assistantservice.application.errors.BookNotFound;
import mentoring.acomi.assistantservice.application.model.RelatedReadBook;
import mentoring.acomi.assistantservice.application.repositories.BookViewRepository;
import mentoring.acomi.assistantservice.application.services.semantic.SemanticSearchService;
import mentoring.acomi.assistantservice.application.view.BookView;

@Component
public class ReadingPathContextBuilder {

	private final BookViewRepository bookRepository;
	private final SemanticSearchService semanticSearchService;

	public ReadingPathContextBuilder(BookViewRepository bookRepository, SemanticSearchService semanticSearchService) {
		this.bookRepository = bookRepository;
		this.semanticSearchService = semanticSearchService;
	}

	public ReadingPathContext build(String isbn) {

		BookView sourceBook = bookRepository.findByIsbn(isbn)
				.orElseThrow(() -> new BookNotFound(String.format("Book not found %s", isbn)));

		List<RelatedReadBook> relatedBooks = semanticSearchService.findSimilarBookCandidates(isbn).candidates();
		List<BookView> candidates = relatedBooks.stream().map(RelatedReadBook::book).toList();

		List<BookView> pathBooks = new ArrayList<>();
	    pathBooks.add(sourceBook);
	    pathBooks.addAll(candidates);
	    
		return new ReadingPathContext(sourceBook, pathBooks.stream().distinct().toList());
	}
}
