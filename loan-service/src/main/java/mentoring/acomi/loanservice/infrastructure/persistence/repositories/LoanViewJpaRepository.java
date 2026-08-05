package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.dto.LoanDto;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanViewEntity;

@Repository
public interface LoanViewJpaRepository extends JpaRepository<LoanViewEntity, String> {
	@Modifying
	@Transactional
	@Query("UPDATE LoanViewEntity l SET l.status = :status, l.updatedAt = :updatedAt WHERE l.id = :id")
	int updateStatus(@Param("id") String id, @Param("status") LoanStatus status, @Param("updatedAt") Instant updatedAt);
	@Query("""
		    SELECT new mentoring.acomi.loanservice.application.dto.LoanDto(
							    l.id,
							    l.isbn,
							    b.author, 
							    b.title,
							    l.userId,
							    u.cardNumber,
							    l.endDate,
							    l.status,
							    CASE
							        WHEN l.status = mentoring.acomi.loanservice.domain.model.LoanStatus.CONFIRMED
							             AND l.endDate < :today
							             AND l.returnedAt IS NULL
							        THEN true
							        ELSE false
							    END
							)
							FROM LoanViewEntity l
							LEFT JOIN UserViewEntity u
							    ON u.id = l.userId
							LEFT JOIN BookViewEntity b 
							    ON l.isbn = b.isbn
							WHERE (:isbn IS NULL OR l.isbn = :isbn)
							  AND (:userId IS NULL OR l.userId = :userId)
							  AND (:status IS NULL OR l.status = :status)
							  AND (:cardNumber IS NULL OR u.cardNumber = :cardNumber)
		""")
	List<LoanDto> findByFilter(@Param("isbn") String isbn, @Param("userId") String userId,  @Param("status") LoanStatus status,
		        @Param("cardNumber") String cardNumber, @Param("today") LocalDate today);
	
	@Modifying
	@Transactional
	@Query("""
			UPDATE LoanViewEntity l 
			SET l.status = mentoring.acomi.loanservice.domain.model.LoanStatus.RETURNED, l.returnedAt = :returnedAt, 
			l.updatedAt = :updatedAt WHERE l.id = :id
			""")
	void returnLoan(@Param("id") String id, @Param("updatedAt") Instant updatedAt, @Param("returnedAt") LocalDate returnedAt);
	
	@Query("""
		    SELECT new mentoring.acomi.loanservice.application.dto.LoanDto(l.id, l.isbn, b.author, b.title,l.userId, u.cardNumber, l.endDate, 
		    l.status, true)
				  FROM LoanViewEntity l
					  LEFT JOIN UserViewEntity u
					      ON u.id = l.userId
					  LEFT JOIN BookViewEntity b 
							    ON l.isbn = b.isbn
					    WHERE l.status = :status AND l.endDate < :dueDate AND l.returnedAt IS NULL
		""")
	List<LoanDto> getLoansOverdue(@Param("dueDate") LocalDate dueDate, @Param("status") LoanStatus status);
		
}
