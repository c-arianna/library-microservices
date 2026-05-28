package mentoring.acomi.bookservice.domain.events.payload;

import mentoring.acomi.bookservice.domain.events.BookReservationRejectReason;

public record BookReservationRejectedPayload(String isbn, String loanId, String userId, BookReservationRejectReason reason) {}
