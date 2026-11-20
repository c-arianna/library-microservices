package mentoring.acomi.assistantservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.view.BookView;
import mentoring.acomi.assistantservice.infrastructure.persistence.entity.BookViewEntity;

@Component
public class BookViewJpaMapper implements ViewMapper<BookView, BookViewEntity>{

	public BookViewEntity toEntity(BookView book) {
		return new BookViewEntity(book.isbn(), book.author(), book.title(), book.description(), book.totalCopies(), 
				book.borrowedCopies(), book.reservedCopies());
	}
	
	public BookView toView(BookViewEntity entity) {
		
		if(entity == null) {
			return null;
		}
		
		return new BookView(entity.getIsbn(), entity.getAuthor(), entity.getTitle(), entity.getDescription(),
				entity.getTotalCopies(), entity.getBorrowedCopies(), entity.getReservedCopies());	
	}

}
