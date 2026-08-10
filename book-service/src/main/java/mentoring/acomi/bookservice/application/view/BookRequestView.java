package mentoring.acomi.bookservice.application.view;

import java.math.BigDecimal;
import java.time.Instant;

import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;

public record BookRequestView(String requestId, String requesterUserId, String author, String title, String isbn, String notes, int votes, 
		BigDecimal estimatedPrice, BookRequestStatus status, Instant createdAt, Instant updatedAt) {

}
