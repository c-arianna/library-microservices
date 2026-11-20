package mentoring.acomi.assistantservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.view.ReaderHistoryView;
import mentoring.acomi.assistantservice.infrastructure.persistence.entity.ReaderHistoryViewEntity;

@Component
public class ReaderHistoryViewMapper implements ViewMapper<ReaderHistoryView, ReaderHistoryViewEntity>{

	public ReaderHistoryViewEntity toEntity(ReaderHistoryView view) {
		return new ReaderHistoryViewEntity(view.loanId(), view.userId(), view.isbn(), view.borrowedAt(), view.returnedAt(),
				view.loanStatus());
	}
	
	public ReaderHistoryView toView(ReaderHistoryViewEntity entity) {
		
		if(entity == null) {
			return null;
		}
		
		return new ReaderHistoryView(entity.getLoanId(), entity.getUserId(), entity.getIsbn(), entity.getBorrowedAt(),
				entity.getReturnedAt(), entity.getLoanStatus());	
	}

}
