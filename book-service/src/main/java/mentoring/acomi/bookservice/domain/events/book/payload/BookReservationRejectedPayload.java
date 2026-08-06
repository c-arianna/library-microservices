package mentoring.acomi.bookservice.domain.events.book.payload;

import mentoring.acomi.bookservice.domain.events.book.BookReservationRejectReason;

public record BookReservationRejectedPayload(String isbn, String loanId, String userId, BookReservationRejectReason reason) {}
