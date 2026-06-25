package mentoring.acomi.bookservice.application.projection;

import java.time.Instant;

import mentoring.acomi.bookservice.application.repositories.BookViewRepository;
import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;

public abstract class AbstractBookProjection {

    protected final BookViewRepository repository;

    protected AbstractBookProjection(BookViewRepository repository) {
        this.repository = repository;
    }

    public void addBook(BookRegisteredIntegrationPayload payload, Instant eventOccurredAt) {
        repository.addBook(getBook(payload), eventOccurredAt);
    }

    public void updateCopies(BookCopiesUpdatedIntegrationPayload payload, Instant eventOccurredAt) {
        repository.updateCopies(payload.isbn(), payload.quantity(), eventOccurredAt);
    }

    public void reserve(BookLoanIntegrationPayload payload, Instant eventOccurredAt) {
        repository.reserve(payload.isbn(), eventOccurredAt);
    }

    public void borrow(BookLoanIntegrationPayload payload, Instant eventOccurredAt) {
        repository.borrow(payload.isbn(), eventOccurredAt);
    }

    public void release(BookLoanIntegrationPayload payload, Instant eventOccurredAt) {
        repository.release(payload.isbn(), eventOccurredAt);
    }

    public void returnBorrowed(BookLoanIntegrationPayload payload, Instant eventOccurredAt) {
        repository.returnBorrowed(payload.isbn(), eventOccurredAt);
    }

    private BookView getBook(BookRegisteredIntegrationPayload payload) {
        return new BookView(payload.isbn(), payload.author(), payload.title(), payload.description(), 0, 0, 0, 0);
    }
}
