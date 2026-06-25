package mentoring.acomi.bookservice.application.repositories;

import java.time.Instant;

import mentoring.acomi.bookservice.application.view.BookView;

public interface BookViewRepository {
	  public void addBook(BookView book, Instant createdAt);
	  public void updateCopies(String isbn, int quantity, Instant updatedAt);
	  public void reserve(String isbn, Instant updatedAt);
	  public void borrow(String isbn, Instant updatedAt);
	  public void release(String isbn, Instant updatedAt);
	  public void returnBorrowed(String isbn, Instant updatedAt);
	  public void deleteAll();
}
