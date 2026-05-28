package mentoring.acomi.bookservice.domain.events;

public sealed interface BookProcessEvent extends BookEvent permits BookReservationRejectedEvent, BookBorrowRejectedEvent{}
