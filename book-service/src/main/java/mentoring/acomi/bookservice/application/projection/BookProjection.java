package mentoring.acomi.bookservice.application.projection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.application.repositories.BookViewRepository;
import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.bookservice.domain.events.BookBorrowedEvent;
import mentoring.acomi.bookservice.domain.events.BookCopiesAddedEvent;
import mentoring.acomi.bookservice.domain.events.BookCopiesRemovedEvent;
import mentoring.acomi.bookservice.domain.events.BookRegisteredEvent;
import mentoring.acomi.bookservice.domain.events.BookReleasedEvent;
import mentoring.acomi.bookservice.domain.events.BookReservedEvent;
import mentoring.acomi.bookservice.domain.events.BookReturnedEvent;
import mentoring.acomi.bookservice.domain.events.BookStateEvent;
import mentoring.acomi.bookservice.domain.events.payload.BookRegisteredPayload;

@Component
public class BookProjection {

	private final BookViewRepository repository;

	public BookProjection(BookViewRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void updateView(BookStateEvent event) {

		switch (event) {
			case BookRegisteredEvent e -> repository.addBook(getBook(e.payload()));
			case BookCopiesAddedEvent e -> repository.addCopies(e.payload().isbn(), e.payload().quantity());
			case BookCopiesRemovedEvent e -> repository.removeCopies(e.payload().isbn(), e.payload().quantity());
			case BookReservedEvent e -> repository.reserve(e.payload().isbn());
			case BookBorrowedEvent e -> repository.borrow(e.payload().isbn());
			case BookReleasedEvent e -> repository.release(e.payload().isbn());
			case BookReturnedEvent e -> repository.returnBorrowed(e.payload().isbn());
		}

	}

	private BookView getBook(BookRegisteredPayload payload) {
		return new BookView(payload.isbn(), payload.author(), payload.title(), payload.description(), 0, 0, 0, 0 );
	}
}