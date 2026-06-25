package mentoring.acomi.bookservice.application.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.bookservice.application.BookFilter;
import mentoring.acomi.bookservice.application.view.BookView;

public interface BookViewQueryRepository {
	public List<BookView> find(BookFilter filter);
	public Optional<BookView> findById(String isbn);
}
