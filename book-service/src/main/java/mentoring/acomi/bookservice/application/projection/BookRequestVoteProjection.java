package mentoring.acomi.bookservice.application.projection;

import java.time.Instant;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.repositories.BookRequestVoteViewRepository;
import mentoring.acomi.bookservice.application.view.BookRequestVoteView;

@Component
public class BookRequestVoteProjection implements BookRequestVoteProjectionOperations {

	private final BookRequestVoteViewRepository repository;
	
	public BookRequestVoteProjection(BookRequestVoteViewRepository repository) {
		this.repository = repository;
	}

	@Override
	public void add(String requestId, String userId, Instant occurredAt) {
		BookRequestVoteView view = new BookRequestVoteView(requestId, userId, occurredAt);
		repository.add(view);
	}

}
