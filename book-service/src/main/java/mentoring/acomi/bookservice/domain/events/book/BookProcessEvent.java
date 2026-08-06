package mentoring.acomi.bookservice.domain.events.book;

public sealed interface BookProcessEvent extends BookEvent permits BookReservationRejectedEvent, BookBorrowRejectedEvent{}
