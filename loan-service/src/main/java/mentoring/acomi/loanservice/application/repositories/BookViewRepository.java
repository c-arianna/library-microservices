package mentoring.acomi.loanservice.application.repositories;

import java.util.Optional;

import mentoring.acomi.loanservice.application.view.BookView;

public interface BookViewRepository {
	public void insert(BookView view);
	public Optional<BookView> findByIsbn(String isbn);
	void deleteAll();
}
