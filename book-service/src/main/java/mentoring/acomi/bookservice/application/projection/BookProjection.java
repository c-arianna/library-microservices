package mentoring.acomi.bookservice.application.projection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.application.repositories.BookViewRepository;
import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;

@Component
public class BookProjection {

	private final BookViewRepository repository;

	public BookProjection(BookViewRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void addBook(BookRegisteredIntegrationPayload payload) {
		repository.addBook(getBook(payload));
	}
	
	@Transactional
	public void updateCopies(BookCopiesUpdatedIntegrationPayload payload) {
		repository.updateCopies(payload.isbn(), payload.quantity());
	}
	
	@Transactional
	public void reserve(BookLoanIntegrationPayload payload) {
		repository.reserve(payload.isbn());
	}
	
	@Transactional
	public void borrow(BookLoanIntegrationPayload payload) {
		repository.borrow(payload.isbn());
	}
	
	@Transactional
	public void release(BookLoanIntegrationPayload payload) {
		repository.release(payload.isbn());
	}
	
	@Transactional
	public void returnBorrowed(BookLoanIntegrationPayload payload) {
		repository.returnBorrowed(payload.isbn());
	}
	
	private BookView getBook(BookRegisteredIntegrationPayload payload) {
		return new BookView(payload.isbn(), payload.author(), payload.title(), payload.description(), 0, 0, 0, 0 );
	}
}