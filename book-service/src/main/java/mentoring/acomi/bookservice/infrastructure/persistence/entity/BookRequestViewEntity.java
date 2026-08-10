package mentoring.acomi.bookservice.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;

@Entity
@Table(name = "book_request_view")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookRequestViewEntity {
	
	@Id
	private String requestId;
	
	private String requesterUserId;
	
	private String author;
	
	private String title;
	
	private String isbn;
	
	private String notes;

	private int votes;
	
	@Enumerated(EnumType.STRING)
	private BookRequestStatus status;
	
	private BigDecimal estimatedPrice;
	
	private Instant createdAt;
	
	private Instant updatedAt;

	public BookRequestViewEntity(String requestId, String requesterUserId, String author, String title, String isbn, String notes, int votes, 
			BigDecimal estimatedPrice, BookRequestStatus status, Instant createdAt, Instant updatedAt) {
		this.requestId = requestId;
		this.requesterUserId = requesterUserId;
		this.author = author;
		this.title = title;
		this.isbn = isbn;
		this.notes = notes;
		this.votes = votes;
		this.estimatedPrice = estimatedPrice;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

}
