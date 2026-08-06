package mentoring.acomi.bookservice.domain.events.book;

import mentoring.acomi.bookservice.domain.events.ProducerEventType;

public enum BookEventType implements ProducerEventType {
	
	BookRegistered, BookCopiesAdded, BookCopiesRemoved, BookReserved, BookBorrowed, BookReleased, BookReturned, 
	BookReservationRejected, BookBorrowRejected;
	
	@Override
    public String eventName() {
        return name();
    }
}
