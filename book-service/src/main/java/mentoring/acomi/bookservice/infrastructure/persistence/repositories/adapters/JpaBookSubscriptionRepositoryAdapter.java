package mentoring.acomi.bookservice.infrastructure.persistence.repositories.adapters;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.application.repositories.BookSubscriptionRepository;
import mentoring.acomi.bookservice.application.view.BookSubscriptionView;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookSubscriptionEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.mapper.BookSubscriptionJpaMapper;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookSubscriptionJpaRepository;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.BookSubscriptionStatus;

@Repository
public class JpaBookSubscriptionRepositoryAdapter implements BookSubscriptionRepository {

	private final BookSubscriptionJpaRepository repository;
	private final BookSubscriptionJpaMapper mapper;
	
	public JpaBookSubscriptionRepositoryAdapter(BookSubscriptionJpaRepository repository, BookSubscriptionJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void add(BookSubscriptionView subscription) {
		BookSubscriptionEntity entity = mapper.toEntity(subscription);
		repository.save(entity);
	}

	@Override
	public void markAsNotified(long subscriptionId, Instant notifyAt) {
		repository.notifySubscription(subscriptionId, BookSubscriptionStatus.NOTIFIED, notifyAt);
	}

	@Override
	public List<BookSubscriptionView> findSubscriptionsToNotify(String isbn) {
		return repository.findSubscriptionToNotify(isbn).stream().map(mapper::toDomain).toList();
	}

	@Override
	public List<BookSubscriptionView> getSubscriptions(String isbn) {
		return repository.findByIsbn(isbn).stream().map(mapper::toDomain).toList();
	}

}
