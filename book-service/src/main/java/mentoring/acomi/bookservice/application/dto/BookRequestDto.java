package mentoring.acomi.bookservice.application.dto;

import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;

public record BookRequestDto(String requestId, String requesterUserId, String author, String title, String isbn, int votes, 
		BookRequestStatus status) {}
