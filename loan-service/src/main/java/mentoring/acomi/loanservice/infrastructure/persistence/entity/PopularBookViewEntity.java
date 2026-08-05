package mentoring.acomi.loanservice.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "popular_book_view")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularBookViewEntity {

	@Id
	private String isbn;

	private String author;
	private String title;
	
	private int loanCount;
	
	public PopularBookViewEntity(String isbn, String author, String title, int loanCount) {
		this.isbn = isbn;
		this.author = author;
		this.title = title;
		this.loanCount = loanCount;
	}
}
