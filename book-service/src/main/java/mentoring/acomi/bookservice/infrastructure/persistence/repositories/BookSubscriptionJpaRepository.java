package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookSubscriptionEntity;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.BookSubscriptionStatus;

public interface BookSubscriptionJpaRepository extends JpaRepository<BookSubscriptionEntity, Long> {

	@Transactional
	@Modifying
	@Query("""
			UPDATE BookSubscriptionEntity bs
				SET bs.status = :status,
				    bs.notifiedAt = :notifyAt
				 WHERE bs.id = :subscriptionId
			""")
	void notifySubscription(long subscriptionId, BookSubscriptionStatus status, Instant notifyAt);

	@Query("""
		    SELECT bs
		      FROM BookSubscriptionEntity bs
		     WHERE bs.isbn = :isbn
		       AND bs.status = mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.BookSubscriptionStatus.ACTIVE
		     ORDER BY bs.createdAt
		""")
	List<BookSubscriptionEntity> findSubscriptionToNotify(String isbn);

	List<BookSubscriptionEntity> findByIsbn(String isbn);
	
}
