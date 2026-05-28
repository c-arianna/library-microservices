package mentoring.acomi.bookservice.domain.events.payload;

import mentoring.acomi.bookservice.domain.events.BookBorrowRejectReason;

public record BookBorrowRejectedPayload(String isbn, String loanId, String userId, BookBorrowRejectReason reason) {}
