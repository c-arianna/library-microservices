package mentoring.acomi.bookservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.view.BookRequestVoteView;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookRequestVoteViewEntity;

@Component
public class BookRequestVoteViewJpaMapper {

	public BookRequestVoteViewEntity toEntity(BookRequestVoteView view) {
		return new BookRequestVoteViewEntity(view.requestId(), view.userId(), view.createdAt());
	}
	
	public BookRequestVoteView toView(BookRequestVoteViewEntity entity) {
		
		if(entity == null) {
			return null;
		}
		
		return new BookRequestVoteView(entity.getRequestId(), entity.getUserId(), entity.getCreatedAt());	
	}
}
