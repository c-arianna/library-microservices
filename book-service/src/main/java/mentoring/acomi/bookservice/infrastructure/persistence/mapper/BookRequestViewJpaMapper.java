package mentoring.acomi.bookservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.view.BookRequestView;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookRequestViewEntity;

@Component
public class BookRequestViewJpaMapper {
	
	public BookRequestViewEntity toEntity(BookRequestView view) {
		return new BookRequestViewEntity(view.requestId(), view.requesterUserId(), view.author(), view.title(), view.isbn(),
				view.notes(), view.votes(), view.status(), view.createdAt(), view.updatedAt());
	}
	
	public BookRequestView toView(BookRequestViewEntity entity) {
		
		if(entity == null) {
			return null;
		}
		
		return new BookRequestView(entity.getRequestId(), entity.getRequesterUserId(), entity.getAuthor(), entity.getTitle(),
				entity.getIsbn(), entity.getNotes(), entity.getVotes(), entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());	
	}

}
