package mentoring.acomi.bookservice.application.dto;

import java.math.BigDecimal;
import java.util.List;

import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;

public record BookRequestDetailDto(String requestId, String requesterUserId, String cardNumber, String isbn, String author, String title,
		String notes, BigDecimal estimatedPrice, BookRequestStatus status, int votes, boolean canVote, List<BookRequestVoteDto> bookRequestVotes) {}
