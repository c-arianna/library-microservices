package mentoring.acomi.bookservice.application.projection;

import java.time.Instant;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.repositories.BookViewRepository;
import mentoring.acomi.bookservice.application.view.BookView;

@Component
public class BookProjection implements BookProjectionOperations {

	private final BookViewRepository repository;

	public BookProjection(BookViewRepository repository) {
	        this.repository = repository;
	}

	@Override
	public void addBook(BookView view, Instant eventOccurredAt) {
		repository.addBook(view, eventOccurredAt);
	}

	@Override
	public void updateCopies(String isbn, int quantity, Instant eventOccurredAt) {
		repository.updateCopies(isbn, quantity, eventOccurredAt);
	}

	@Override
	public void reserve(String isbn, Instant eventOccurredAt) {
		repository.reserve(isbn, eventOccurredAt);
	}

	@Override
	public void borrow(String isbn, Instant eventOccurredAt) {
		repository.borrow(isbn, eventOccurredAt);
	}

	@Override
	public void release(String isbn, Instant eventOccurredAt) {
		repository.release(isbn, eventOccurredAt);
	}

	@Override
	public void returnBorrowed(String isbn, Instant eventOccurredAt) {
		repository.returnBorrowed(isbn, eventOccurredAt);
	}

}
