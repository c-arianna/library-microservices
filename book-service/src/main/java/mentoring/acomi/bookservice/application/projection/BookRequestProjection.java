package mentoring.acomi.bookservice.application.projection;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.repositories.BookRequestViewRepository;
import mentoring.acomi.bookservice.application.view.BookRequestView;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;

@Component
public class BookRequestProjection implements BookRequestProjectionOperations{

	private final BookRequestViewRepository repository;
	
	public BookRequestProjection(BookRequestViewRepository repository) {
		this.repository = repository;
	}

	@Override
	public void add(BookRequestView view) {
		repository.add(view);	
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
	
	@Override
	public void updatePrice(String requestId, BigDecimal estimatedPrice, Instant occurredA) {
		repository.updatePrice(requestId, estimatedPrice, occurredA);		
	}
	
}
