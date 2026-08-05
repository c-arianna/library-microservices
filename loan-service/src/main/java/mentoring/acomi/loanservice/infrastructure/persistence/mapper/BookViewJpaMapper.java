package mentoring.acomi.loanservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.view.BookView;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.BookViewEntity;

@Component
public class BookViewJpaMapper {

	public BookViewEntity toEntity(BookView book) {
		return new BookViewEntity(book.isbn(), book.author(), book.title());
	}

	public BookView toView(BookViewEntity entity) {

		if (entity == null) {
			return null;
		}

		return new BookView(entity.getIsbn(), entity.getAuthor(), entity.getTitle());
	}
}
