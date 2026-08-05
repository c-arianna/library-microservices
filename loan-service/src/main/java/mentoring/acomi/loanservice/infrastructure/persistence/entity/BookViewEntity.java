package mentoring.acomi.loanservice.infrastructure.persistence.entity;

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
	
	public BookViewEntity(String isbn, String author, String title) {
		this.isbn = isbn;
		this.author = author;
		this.title = title;
	}

}
