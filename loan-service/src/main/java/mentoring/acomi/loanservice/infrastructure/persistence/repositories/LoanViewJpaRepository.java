package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.dto.LoanDto;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanViewEntity;

@Repository
public interface LoanViewJpaRepository extends JpaRepository<LoanViewEntity, String> {
	@Modifying
	@Query("UPDATE LoanViewEntity l SET l.status = :status, l.updatedAt = :updatedAt WHERE l.id = :id")
	int updateStatus(@Param("id") String id, @Param("status") LoanStatus status, @Param("updatedAt") Instant updatedAt);
	@Query("""
		    SELECT new mentoring.acomi.loanservice.infrastructure.dto.LoanDto(l.id, l.isbn, l.userId, u.cardNumber, l.status)
				  FROM LoanViewEntity l
					  LEFT JOIN UserViewEntity u
					      ON u.id = l.userId
					    WHERE l.userId = u.id
						      AND (:isbn IS NULL OR l.isbn = :isbn)
						      AND (:userId IS NULL OR l.userId = :userId)
						      AND (:status IS NULL OR l.status = :status)
						      AND (:cardNumber IS NULL OR u.cardNumber = :cardNumber)
		""")
	List<LoanDto> findByFilter(@Param("isbn") String isbn, @Param("userId") String userId,  @Param("status") LoanStatus status,
		        @Param("cardNumber") String cardNumber);
}
