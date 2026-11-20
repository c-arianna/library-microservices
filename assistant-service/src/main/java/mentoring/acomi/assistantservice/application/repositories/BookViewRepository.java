package mentoring.acomi.assistantservice.application.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.assistantservice.application.view.BookView;

public interface BookViewRepository extends ViewRepository<BookView>{
	Optional<BookView> findByIsbn(String isbn);
	List<BookView> findAvailableBooks();
	List<BookView> findMostPopularBooks(int limit);
}
