package mentoring.acomi.assistantservice.application.context;

import java.util.List;

import mentoring.acomi.assistantservice.application.view.BookView;

public record SimilarBooksContext(BookView sourceBook, List<BookView> candidateBooks) {
	
}
