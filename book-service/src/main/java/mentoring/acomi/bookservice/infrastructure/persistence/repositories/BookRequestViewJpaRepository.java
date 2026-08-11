package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.application.view.BookRequestView;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookRequestViewEntity;

public interface BookRequestViewJpaRepository extends JpaRepository<BookRequestViewEntity, String>{

	@Modifying
	@Transactional
	@Query("UPDATE BookRequestViewEntity e set e.votes = e.votes + :votes, e.updatedAt = :updatedAt  where e.requestId = :requestId")
	void registerVotes(@Param("requestId") String requestId, @Param("votes") int votes, @Param("updatedAt") Instant updatedAt);
	
	@Modifying
	@Transactional
	@Query("UPDATE BookRequestViewEntity e set e.status = :status, e.updatedAt = :updatedAt where e.requestId = :requestId")
	void updateStatus(@Param("requestId") String requestId, @Param("status") BookRequestStatus status, @Param("updatedAt") Instant updatedAt);
	
	@Query("""
			SELECT b 
			   FROM BookRequestViewEntity b 
			      WHERE b.isbn = :isbn and b.status = mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus.PENDING
		    """)
	Optional<BookRequestViewEntity> findPendingRequestByIsbn(@Param("isbn") String isbn);
	@Query("""
			SELECT b 
			   FROM BookRequestViewEntity b 
			      WHERE lower(b.author) = lower(:author) and lower(b.title) = lower(:title) 
			      and b.status = mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus.PENDING
		    """)
	Optional<BookRequestViewEntity> findPendingRequestByAuthorAndTitle(@Param("author") String author, @Param("title") String title);
	
	@Modifying
	@Transactional
	@Query("UPDATE BookRequestViewEntity e set e.estimatedPrice = :estimatedPrice, e.updatedAt = :updatedAt where e.requestId = :requestId")
	void updatePrice(@Param("requestId") String requestId, @Param("estimatedPrice") BigDecimal estimatedPrice, 
			@Param("updatedAt") Instant updatedAt);
	
	@Query("""
			SELECT b 
			   FROM BookRequestViewEntity b 
			      WHERE b.estimatedPrice IS NOT NULL and 
			      b.status = mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus.PENDING
		    """)
	List<BookRequestView> findPurchasableRequests();

	long countByStatus(BookRequestStatus status);
}
