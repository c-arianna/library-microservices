package mentoring.acomi.bookservice.application.aggregates;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import mentoring.acomi.bookservice.domain.errors.BookNotRegistered;
import mentoring.acomi.bookservice.domain.errors.CannotRemoveBookCopies;
import mentoring.acomi.bookservice.domain.errors.InvalidIsbn;
import mentoring.acomi.bookservice.domain.errors.InvalidQuantity;
import mentoring.acomi.bookservice.domain.events.BookBorrowRejectReason;
import mentoring.acomi.bookservice.domain.events.BookBorrowRejectedEvent;
import mentoring.acomi.bookservice.domain.events.BookBorrowedEvent;
import mentoring.acomi.bookservice.domain.events.BookCopiesAddedEvent;
import mentoring.acomi.bookservice.domain.events.BookCopiesRemovedEvent;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.domain.events.BookRegisteredEvent;
import mentoring.acomi.bookservice.domain.events.BookReleasedEvent;
import mentoring.acomi.bookservice.domain.events.BookReservationRejectReason;
import mentoring.acomi.bookservice.domain.events.BookReservationRejectedEvent;
import mentoring.acomi.bookservice.domain.events.BookReservedEvent;
import mentoring.acomi.bookservice.domain.events.BookReturnedEvent;
import mentoring.acomi.bookservice.domain.events.payload.BookBorrowRejectedPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookCopiesAddedPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookCopiesRemovedPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookLoanPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookRegisteredPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookReservationRejectedPayload;
import mentoring.acomi.bookservice.domain.model.Book;
import mentoring.acomi.bookservice.domain.model.ISBN;

public class BookAggregate {

	private Consumer<BookEvent> dispatcher;

	private ISBN id;
	private boolean isRegistered = false;
	private int totalCopies = 0;
	private int borrowed = 0;
	private int reserved = 0;

	private int version = -1;

	private Set<String> reservedLoans = new HashSet<>();
	private Set<String> borrowedLoans = new HashSet<>();

	public BookAggregate(ISBN id, Consumer<BookEvent> dispatcher, List<BookEvent> events) {
		this.id = id;
		this.dispatcher = dispatcher;
		replay(events);
	}

	private void replay(List<BookEvent> events) {
		for (BookEvent event : events) {
			apply(event);
		}
	}

	public void apply(BookEvent event) {

		int expectedVersion = version + 1;
		if (event.eventVersion() != expectedVersion) {
			throw new IllegalStateException(String.format("Invalid event version, expected %d, actual %d", expectedVersion, event.eventVersion()));
		}

		switch (event) {
			case BookRegisteredEvent e -> applyBookEventRegistered(e);
			case BookCopiesAddedEvent e -> applyBookCopiesAdded(e);
			case BookCopiesRemovedEvent e -> applyBookCopiesRemoved(e);
			case BookReservedEvent e -> applyBookReserved(e);
			case BookBorrowedEvent e -> applyBookBorrowed(e);
			case BookReleasedEvent e -> applyBookReleased(e);
			case BookReturnedEvent e -> applyBookReturned(e);
			case BookReservationRejectedEvent e -> {}
			case BookBorrowRejectedEvent e -> {}
		}

		version++;

	}

	private void applyBookEventRegistered(BookRegisteredEvent event) {
		isRegistered = true;
	}

	private void applyBookCopiesAdded(BookCopiesAddedEvent event) {
		totalCopies += event.payload().quantity();
	}

	private void applyBookCopiesRemoved(BookCopiesRemovedEvent event) {
		totalCopies -= event.payload().quantity();
	}

	private void applyBookReserved(BookReservedEvent event) {

		String loanId = event.payload().loanId();
		if (!reservedLoans.contains(loanId) && !borrowedLoans.contains(loanId)) {
			reservedLoans.add(loanId);
			reserved += 1;
		}
	}

	private void applyBookBorrowed(BookBorrowedEvent event) {
		String loanId = event.payload().loanId();

		if (!borrowedLoans.contains(loanId)) {
			if (reservedLoans.contains(loanId)) {
				reservedLoans.remove(loanId);
				reserved = Math.max(0, reserved - 1);
			}
			borrowedLoans.add(loanId);
			borrowed += 1;
		}
	}

	private void applyBookReleased(BookReleasedEvent event) {

		String loanId = event.payload().loanId();
		if (reservedLoans.contains(loanId)) {
			reservedLoans.remove(loanId);
			reserved = Math.max(0, reserved - 1);
		}
	}

	private void applyBookReturned(BookReturnedEvent event) {

		String loanId = event.payload().loanId();

		if (borrowedLoans.contains(loanId)) {
			borrowedLoans.remove(loanId);
			borrowed = Math.max(0, borrowed - 1);
		}
	}

	public void register(Book book) {

		if (!book.getIsbn().equals(id.getValue())) {
			throw new InvalidIsbn("Book ISBN does not match aggregate id");
		}

		if (!isRegistered) {
			BookRegisteredPayload payload = new BookRegisteredPayload(book.getIsbn(), book.getAuthor(), book.getTitle(),
					book.getDescription());
			BookRegisteredEvent event = new BookRegisteredEvent(book.getIsbn(), getEventId(), nextVersion(), payload,
					Instant.now());
			manageEvent(event);
		}

	}

	public void addCopies(int quantity) {

		ensureRegistered();

		if (quantity <= 0) {
			throw new InvalidQuantity("quantity must be > 0");
		}

		BookCopiesAddedEvent event = new BookCopiesAddedEvent(id.getValue(), getEventId(), nextVersion(),
				new BookCopiesAddedPayload(id.getValue(), quantity), Instant.now());
		manageEvent(event);

	}

	public void removeCopies(int quantity, String reason) {

		ensureRegistered();

		if (quantity <= 0) {
			throw new InvalidQuantity("quantity must be > 0");
		}

		int copiesAvailable = totalCopies - (borrowed + reserved);
		if (copiesAvailable < quantity) {
			throw new CannotRemoveBookCopies(
					String.format("Cannot remove %d copies, total copies available %d", quantity, copiesAvailable));
		}

		BookCopiesRemovedEvent event = new BookCopiesRemovedEvent(id.getValue(), getEventId(), nextVersion(),
				new BookCopiesRemovedPayload(id.getValue(), quantity, reason), Instant.now());
		manageEvent(event);

	}

	public void reserve(String loanId, String userId) {

		if (!isRegistered || availableCopies() <= 0) {

			BookReservationRejectReason reason = availableCopies() <= 0 ? BookReservationRejectReason.BOOK_NOT_AVAILABLE
					: BookReservationRejectReason.BOOK_NOT_REGISTERED;

			BookReservationRejectedEvent event = new BookReservationRejectedEvent(id.getValue(), getEventId(),
					nextVersion(), new BookReservationRejectedPayload(id.getValue(), loanId, userId, reason),
					Instant.now());
			manageEvent(event);
			return;
		}

		if (!reservedLoans.contains(loanId) && !borrowedLoans.contains(loanId)) {
			BookReservedEvent event = new BookReservedEvent(id.getValue(), getEventId(), nextVersion(),
					new BookLoanPayload(id.getValue(), loanId, userId), Instant.now());
			manageEvent(event);
		}
	}

	public void borrow(String loanId, String userId) {

		if (!isRegistered) {
			BookBorrowRejectedEvent event = new BookBorrowRejectedEvent(id.getValue(), getEventId(), nextVersion(),
					new BookBorrowRejectedPayload(id.getValue(), loanId, userId,
							BookBorrowRejectReason.BOOK_NOT_REGISTERED),
					Instant.now());
			manageEvent(event);
			return;
		}

		if (!borrowedLoans.contains(loanId)) {

			if (!reservedLoans.contains(loanId)) {
				BookBorrowRejectedEvent event = new BookBorrowRejectedEvent(id.getValue(), getEventId(), nextVersion(),
						new BookBorrowRejectedPayload(id.getValue(), loanId, userId,
								BookBorrowRejectReason.RESERVATION_MISSING),
						Instant.now());
				manageEvent(event);
				return;
			}

			BookBorrowedEvent event = new BookBorrowedEvent(id.getValue(), getEventId(), nextVersion(),
					new BookLoanPayload(id.getValue(), loanId, userId), Instant.now());
			manageEvent(event);

		}
	}

	public void release(String loanId, String userId) {

		ensureRegistered();

		if (reservedLoans.contains(loanId)) {
			BookReleasedEvent event = new BookReleasedEvent(id.getValue(), getEventId(), nextVersion(),
					new BookLoanPayload(id.getValue(), loanId, userId), Instant.now());
			manageEvent(event);
		}

	}

	public void returnBorrowed(String loanId, String userId) {

		ensureRegistered();

		if (borrowedLoans.contains(loanId)) {

			BookReturnedEvent event = new BookReturnedEvent(id.getValue(), getEventId(), nextVersion(),
					new BookLoanPayload(id.getValue(), loanId, userId), Instant.now());
			manageEvent(event);
		}

	}

	private void ensureRegistered() {
		if (!isRegistered) {
			throw new BookNotRegistered(String.format("Book not registered, ISBN: %s", id));
		}

	}

	private int availableCopies() {
		return totalCopies - reserved - borrowed;
	}

	private void manageEvent(BookEvent event) {
		apply(event);
		dispatcher.accept(event);
	}

	private String getEventId() {
		return UUID.randomUUID().toString();
	}

	private int nextVersion() {
		return version + 1;
	}

}
