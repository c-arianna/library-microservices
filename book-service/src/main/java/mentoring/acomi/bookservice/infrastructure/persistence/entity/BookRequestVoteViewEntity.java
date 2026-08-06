package mentoring.acomi.bookservice.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_request_vote_view")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookRequestVoteViewEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String requestId;
	
	private String userId;
	
	private Instant createdAt;

	public BookRequestVoteViewEntity(String requestId, String userId, Instant createdAt) {
		this.requestId = requestId;
		this.userId = userId;
		this.createdAt = createdAt;
	}
		
}
