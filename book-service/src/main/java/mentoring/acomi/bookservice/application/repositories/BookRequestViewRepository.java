package mentoring.acomi.bookservice.application.repositories;

import java.time.Instant;

import mentoring.acomi.bookservice.application.view.BookRequestView;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;

public interface BookRequestViewRepository {
	void add(BookRequestView view);
	void registerVotes(String requestId, int votes, Instant updatedAt);
	void updateStatus(String requestId, BookRequestStatus status, Instant updatedAt);
	void deleteAll();
}
