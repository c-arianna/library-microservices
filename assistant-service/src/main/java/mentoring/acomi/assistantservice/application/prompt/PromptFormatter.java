package mentoring.acomi.assistantservice.application.prompt;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.model.ReadingPathStep;
import mentoring.acomi.assistantservice.application.model.RelatedReadBook;
import mentoring.acomi.assistantservice.application.view.BookView;

@Component
public class PromptFormatter {

    public String formatBook(BookView book) {
        return """
                ISBN: %s
                Titolo: %s
                Autore: %s
                Descrizione: %s
                """
                .formatted(
                        book.isbn(),
                        book.title(),
                        book.author(),
                        book.description());
    }
    
    public String formatPath(List<ReadingPathStep> path) {
		return path.stream().map(step -> formatStep(step)).collect(Collectors.joining("\n\n"));
	}
	
	public String formatBooks(Map<Integer, BookView> books) {
		return books.entrySet().stream().map(entry -> formatBook(entry.getKey(), entry.getValue()))
				.collect(Collectors.joining("\n\n"));
	}
   
	private String formatStep(ReadingPathStep step) {
		return """
				Titolo: %s
				Autore: %s
				Descrizione: %s
				""".formatted(step.title(), step.author(), step.description());

	}
	
	public String formatRelatedBooks(List<RelatedReadBook> relatedReadBooks) {
		return relatedReadBooks.stream().map(book -> formatRelatedBook(book))
				.collect(Collectors.joining("\n\n"));
	}

	private String formatRelatedBook(RelatedReadBook relatedBook) {
		BookView book = relatedBook.book();
		return """
				Titolo: %s 
				Autore: %s
				Descrizione: %s
				Score similiarità %f""".formatted(book.title(), book.author(), book.description(), relatedBook.score());
	}
	
	private String formatBook(int id, BookView book) {
		return """
				id: %d,
				Titolo: %s
				Autore: %s
				Descrizione: %s
				""".formatted(id, book.title(), book.author(), book.description());

	}
}