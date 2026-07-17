package mentoring.acomi.bookservice.application.projection;

import java.time.Instant;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.repositories.BookViewRepository;
import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;

@Component
public class BookProjection implements BookProjectionOperations {

	private final BookViewRepository repository;

	public BookProjection(BookViewRepository repository) {
	        this.repository = repository;
	}

	@Override
	public void addBook(BookRegisteredIntegrationPayload payload, Instant eventOccurredAt) {
		repository.addBook(getBook(payload), eventOccurredAt);
	}

	@Override
	public void updateCopies(BookCopiesUpdatedIntegrationPayload payload, Instant eventOccurredAt) {
		repository.updateCopies(payload.isbn(), payload.quantity(), eventOccurredAt);
	}

	@Override
	public void reserve(BookLoanIntegrationPayload payload, Instant eventOccurredAt) {
		repository.reserve(payload.isbn(), eventOccurredAt);
	}

	@Override
	public void borrow(BookLoanIntegrationPayload payload, Instant eventOccurredAt) {
		repository.borrow(payload.isbn(), eventOccurredAt);
	}

	@Override
	public void release(BookLoanIntegrationPayload payload, Instant eventOccurredAt) {
		repository.release(payload.isbn(), eventOccurredAt);
	}

	@Override
	public void returnBorrowed(BookLoanIntegrationPayload payload, Instant eventOccurredAt) {
		repository.returnBorrowed(payload.isbn(), eventOccurredAt);
	}

	private BookView getBook(BookRegisteredIntegrationPayload payload) {
		return new BookView(payload.isbn(), payload.author(), payload.title(), payload.description(), 0, 0, 0, 0);
	}
}
