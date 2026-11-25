package mentoring.acomi.assistantservice.application.services.readingpath;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

	public ReadingPathContext buildReadingPathContext(String isbn) {

		BookView sourceBook = bookRepository.findByIsbn(isbn)
				.orElseThrow(() -> new BookNotFound(String.format("Book not found %s", isbn)));

		List<RelatedReadBook> relatedBooks = semanticSearchService.findSimilarBookCandidates(isbn).candidates();
		List<BookView> candidates = relatedBooks.stream().map(RelatedReadBook::book).toList();

		Map<String, BookView> uniqueBooks = new LinkedHashMap<>();

		uniqueBooks.put(sourceBook.isbn(), sourceBook);

		for (BookView candidate : candidates) {
		    uniqueBooks.putIfAbsent(candidate.isbn(), candidate);
		}

		Map<Integer, BookView> pathBooks = new LinkedHashMap<>();

		int id = 1;
		for (BookView book : uniqueBooks.values()) {
		    pathBooks.put(id++, book);
		}
	
		return new ReadingPathContext(sourceBook, pathBooks);
	}
}
