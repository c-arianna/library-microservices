package mentoring.acomi.bookservice.infrastructure.persistence.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_view")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookViewEntity {

	@Id
	private String isbn;

	private String author;
	private String title;
	private String description;
	private int totalCopies;
	private int borrowedCopies;
	private int reservedCopies;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant updatedAt;

	public BookViewEntity(String isbn, String author, String title, String description) {
		this(isbn, author, title, description, 0, 0, 0);
	}

	public BookViewEntity(String isbn, String author, String title, String description, int totalCopies,
			int borrowedCopies, int reservedCopies) {
		this.isbn = isbn;
		this.author = author;
		this.title = title;
		this.description = description;
		this.totalCopies = totalCopies;
		this.borrowedCopies = borrowedCopies;
		this.reservedCopies = reservedCopies;
	}

	public void markCreated(Instant ts) {
		if (this.createdAt != null) {
			throw new IllegalStateException("createdAt already set");
		}
		this.createdAt = ts;
		this.updatedAt = ts;
	}

}
