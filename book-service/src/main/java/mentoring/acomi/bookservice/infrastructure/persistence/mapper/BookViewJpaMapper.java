package mentoring.acomi.bookservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookViewEntity;

@Component
public class BookViewJpaMapper {

	public BookViewEntity toEntity(BookView book) {
		return new BookViewEntity(book.isbn(), book.author(), book.title(), book.description());
	}
	
	public BookView toView(BookViewEntity entity) {
		
		if(entity == null) {
			return null;
		}
		
		int availableCopies = entity.getTotalCopies() - entity.getBorrowedCopies() - entity.getReservedCopies();
		return new BookView(entity.getIsbn(), entity.getAuthor(), entity.getTitle(), entity.getDescription(),
				entity.getTotalCopies(), availableCopies, entity.getBorrowedCopies(),
				entity.getReservedCopies());	
	}

}
