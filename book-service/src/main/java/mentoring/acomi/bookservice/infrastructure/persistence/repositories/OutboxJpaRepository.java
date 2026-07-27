package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.OutboxEventEntity;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxStatus;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventEntity, String> {
	@Query("""
		    SELECT o
		      FROM OutboxEventEntity o
		     WHERE o.status = :status
		       AND o.nextRetryAt <= :now
		     ORDER BY o.createdAt
		""")
		List<OutboxEventEntity> findEventsToPublish(@Param("status") OutboxStatus status, @Param("now") Instant now, 
				Pageable pageable);

	@Transactional
	@Modifying
	@Query("""
			UPDATE OutboxEventEntity o 
				SET o.status = mentoring.acomi.sharedcorelibrary.outbox.OutboxStatus.PUBLISHED,
				    o.retryCount = 0,
				    o.publishedAt = :publishedAt,
				    o.lastError = null,
				    o.nextRetryAt = null
				WHERE o.eventId = :eventId			
			""")
	void published(@Param("eventId") String eventId, @Param("publishedAt") Instant publishedAt);
		
	@Transactional
	@Modifying
	@Query("""
			UPDATE OutboxEventEntity o 
				SET o.status = :status,
				    o.lastError = :lastError,
				    o.retryCount = :retryCount,
				    o.nextRetryAt = :nextRetryAt
				WHERE o.eventId = :eventId			
			""")
	void recordFailure(@Param("eventId") String eventId, @Param("status") OutboxStatus status, 
			@Param("lastError") String lastError, @Param("retryCount") int retryCount, 
			@Param("nextRetryAt") Instant nextRetryAt);
}

