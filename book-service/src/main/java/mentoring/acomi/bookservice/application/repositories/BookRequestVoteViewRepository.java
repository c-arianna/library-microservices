package mentoring.acomi.bookservice.application.repositories;

import mentoring.acomi.bookservice.application.view.BookRequestVoteView;

public interface BookRequestVoteViewRepository {
	void add(BookRequestVoteView view);
	void deleteAll();	
}
