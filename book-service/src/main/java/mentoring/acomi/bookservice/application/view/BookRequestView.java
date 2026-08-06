package mentoring.acomi.bookservice.application.view;

import java.time.Instant;

import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;

public record BookRequestView(String requestId, String requestUserId, String author, String title, String isbn, String notes, int votes, 
		BookRequestStatus status, Instant createdAt, Instant updatedAt) {

}
