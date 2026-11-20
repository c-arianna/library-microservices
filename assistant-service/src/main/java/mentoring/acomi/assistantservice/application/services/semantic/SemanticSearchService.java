package mentoring.acomi.assistantservice.application.services.semantic;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import mentoring.acomi.assistantservice.application.errors.BookNotFound;
import mentoring.acomi.assistantservice.application.model.RelatedReadBook;
import mentoring.acomi.assistantservice.application.model.SemanticSearchResponse;
import mentoring.acomi.assistantservice.application.repositories.BookViewRepository;
import mentoring.acomi.assistantservice.application.view.BookView;

@Service
public class SemanticSearchService {

	private final BookViewRepository bookRepository;
	private final VectorStore vectorStore;

	public SemanticSearchService(BookViewRepository bookRepository, VectorStore vectorStore) {
		this.bookRepository = bookRepository;
		this.vectorStore = vectorStore;
	}

	public SemanticSearchResponse findSimilarBookCandidates(String isbn) {

		BookView source = bookRepository.findByIsbn(isbn)
				.orElseThrow(() -> new BookNotFound(String.format("Book not found, isbn %s", isbn)));

		String content = buildContent(source);

		List<Document> documents = vectorStore
				.similaritySearch(SearchRequest.builder().query(content).topK(10).similarityThreshold(0.0).build());

		List<Document> filtered = documents.stream().filter(d -> !isbn.equals(d.getMetadata().get("isbn"))).toList();

		double bestScore = filtered.stream().findFirst().map(Document::getScore).orElse(0.0);

		double threshold = Math.max(0.72, bestScore * 0.90);

		List<Document> candidates = filtered.stream().filter(d -> d.getScore() >= threshold).toList();

		List<RelatedReadBook> bookCandidates = candidates.stream().map(doc -> {

			String similarIsbn = (String) doc.getMetadata().get("isbn");
			BookView book = bookRepository.findByIsbn(similarIsbn).orElse(null);

			if (book == null) {
				return null;
			}

			return new RelatedReadBook(book, doc.getScore());
		}).filter(Objects::nonNull).filter(candidate -> !candidate.book().isbn().equals(source.isbn()))
				.sorted(Comparator.comparing(RelatedReadBook::score).reversed()).toList();
		
		return new SemanticSearchResponse(source, bookCandidates);
	}

	private String buildContent(BookView book) {

		return """
				Titolo: %s

				Autore: %s

				Descrizione: %s
				""".formatted(book.title(), book.author(), book.description());
	}

}
