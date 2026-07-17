package mentoring.acomi.bookservice.application.projection;

import java.time.Instant;

import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;

public interface BookProjectionOperations {
    void addBook(BookRegisteredIntegrationPayload payload, Instant occurredAt);
    void updateCopies(BookCopiesUpdatedIntegrationPayload payload, Instant occurredAt);
    void reserve(BookLoanIntegrationPayload payload, Instant occurredAt);
    void borrow(BookLoanIntegrationPayload payload, Instant occurredAt);
    void release(BookLoanIntegrationPayload payload, Instant occurredAt);
    void returnBorrowed(BookLoanIntegrationPayload payload, Instant occurredAt);
}