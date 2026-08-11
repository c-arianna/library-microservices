package mentoring.acomi.bookservice.application.projection;

import java.time.Instant;

import mentoring.acomi.bookservice.application.view.BookView;

public interface BookProjectionOperations {
    void addBook(BookView view, Instant occurredAt);
    void updateCopies(String isbn, int quantity, Instant occurredAt);
    void reserve(String isbn, Instant occurredAt);
    void borrow(String isbn, Instant occurredAt);
    void release(String isbn, Instant occurredAt);
    void returnBorrowed(String isbn, Instant occurredAt);
}