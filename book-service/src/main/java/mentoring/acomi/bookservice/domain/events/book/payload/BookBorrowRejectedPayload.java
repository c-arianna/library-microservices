package mentoring.acomi.bookservice.domain.events.book.payload;

import mentoring.acomi.bookservice.domain.events.book.BookBorrowRejectReason;

public record BookBorrowRejectedPayload(String isbn, String loanId, String userId, BookBorrowRejectReason reason) {}
