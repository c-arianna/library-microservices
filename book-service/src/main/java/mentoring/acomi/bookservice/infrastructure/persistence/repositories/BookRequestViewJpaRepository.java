package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookRequestViewEntity;

public interface BookRequestViewJpaRepository extends JpaRepository<BookRequestViewEntity, String>{

	@Modifying
	@Transactional
	@Query("UPDATE BookRequestViewEntity e set e.votes = e.votes + :votes, e.updatedAt = :updatedAt  where e.requestId = :requestId")
	void registerVotes(@Param("requestId") String requestId, @Param("votes") int votes, @Param("updatedAt") Instant updatedAt);
	
	@Modifying
	@Transactional
	@Query("UPDATE BookRequestViewEntity e set e.status = :status, e.updatedAt = :updatedAt where e.requestId = :requestId")
	void updateStatus(@Param("requestId") String requestId, @Param("status") String status, @Param("updatedAt") Instant updatedAt);
	
}
