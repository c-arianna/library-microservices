package mentoring.acomi.assistantservice.application.repositories;

import java.util.List;

import mentoring.acomi.assistantservice.application.view.ReaderHistoryView;

public interface ReaderHistoryViewRepository extends ViewRepository<ReaderHistoryView>{
	List<ReaderHistoryView> findByUserId(String userId);
}
