package mentoring.acomi.bookservice.application.projection;

import java.time.Instant;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.repositories.BookRequestViewRepository;
import mentoring.acomi.bookservice.application.view.BookRequestView;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestAddedIntegrationPayload;

@Component
public class BookRequestProjection implements BookRequestProjectionOperations{

	private final BookRequestViewRepository repository;
	
	public BookRequestProjection(BookRequestViewRepository repository) {
		this.repository = repository;
	}

	@Override
	public void add(BookRequestAddedIntegrationPayload payload, Instant occurredAt) {
		repository.add(getBookRequest(payload, occurredAt));	
	}

	@Override
	public void registerVotes(String requestId, int votes, Instant occurredA) {
		repository.registerVotes(requestId, votes, occurredA);		
	}

	@Override
	public void approve(String requestId, Instant occurredA) {
		repository.updateStatus(requestId, BookRequestStatus.APPROVED, occurredA);		
	}

	@Override
	public void reject(String requestId, Instant occurredA) {
		repository.updateStatus(requestId, BookRequestStatus.REJECTED, occurredA);
	}
	
	private BookRequestView getBookRequest(BookRequestAddedIntegrationPayload payload, Instant occurredAt) {
		return new BookRequestView(payload.requestId(), payload.requesterUserId(), payload.author(), payload.title(), payload.isbn(),
				payload.notes(), 1, BookRequestStatus.PENDING, occurredAt, occurredAt);
	}


}
