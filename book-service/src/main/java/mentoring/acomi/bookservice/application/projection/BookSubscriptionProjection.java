package mentoring.acomi.bookservice.application.projection;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.application.repositories.BookSubscriptionRepository;

@Component
public class BookSubscriptionProjection {

	private final BookSubscriptionRepository repository;

	public BookSubscriptionProjection(BookSubscriptionRepository repository) {
		this.repository = repository;
	}
	
	@Transactional
	public void markAsNotified(long subscriptionId, Instant notifyAt) {
		repository.markAsNotified(subscriptionId, notifyAt);
	}
	
}
