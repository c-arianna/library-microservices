package mentoring.acomi.bookservice.application.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.bookservice.application.BookFilter;
import mentoring.acomi.bookservice.application.view.BookView;

public interface BookViewRepository {
	  public void addBook(BookView book);
	  public List<BookView> find(BookFilter filter);
	  public void addCopies(String isbn, int quantity);
	  public void removeCopies(String isbn, int quantity);
	  public Optional<BookView> findById(String isbn);
	  public void reserve(String isbn);
	  public void borrow(String isbn);
	  public void release(String isbn);
	  public void returnBorrowed(String isbn);
	  public void deleteAll();
}
