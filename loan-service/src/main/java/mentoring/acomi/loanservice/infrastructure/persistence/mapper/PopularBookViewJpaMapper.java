package mentoring.acomi.loanservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.view.PopularBookView;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.PopularBookViewEntity;

@Component
public class PopularBookViewJpaMapper {
	
	public PopularBookViewEntity toEntity(PopularBookView popularBook) {
		return new PopularBookViewEntity(popularBook.isbn(), popularBook.author(), popularBook.title(), popularBook.loanCount());
	}

	public PopularBookView toView(PopularBookViewEntity entity) {

		if (entity == null) {
			return null;
		}

		return new PopularBookView(entity.getIsbn(), entity.getAuthor(), entity.getTitle(), entity.getLoanCount());
	}

}
