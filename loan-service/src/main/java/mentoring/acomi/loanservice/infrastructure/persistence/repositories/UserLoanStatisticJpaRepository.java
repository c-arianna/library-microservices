package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.dto.OverdueStatisticDto;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.UserLoanStatisticEntity;

public interface UserLoanStatisticJpaRepository extends JpaRepository<UserLoanStatisticEntity, String> {
	
	@Query("""
			SELECT u
			   FROM UserLoanStatisticEntity u
			   	 WHERE u.userId = :userId
			""")
	Optional<UserLoanStatisticEntity> getUserLoanStatistic(@Param("userId") String userId);
	
	@Transactional
	@Modifying
	@Query("""
	    UPDATE UserLoanStatisticEntity u
	       SET u.overdueLoansCount = u.overdueLoansCount + 1,
	           u.totalDaysOverdue = u.totalDaysOverdue + :daysOverdue,
	           u.lastOverdueDate =
	                CASE
	                    WHEN u.lastOverdueDate IS NULL
	                      OR :lastOverdueDate > u.lastOverdueDate
	                    THEN :lastOverdueDate
	                    ELSE u.lastOverdueDate
	                END
	     WHERE u.userId = :userId
	""")
	void updateStatistics(@Param("userId") String userId, @Param("daysOverdue") long daysOverdue, 
			@Param("lastOverdueDate") LocalDate lastOverdueDate);
	
	@Query("""
		    SELECT new mentoring.acomi.loanservice.application.dto.OverdueStatisticDto(
		        u.userId,
		        uv.cardNumber,
		        u.overdueLoansCount,
		        count(distinct l.id),
		        u.totalDaysOverdue,
		        u.lastOverdueDate
		    )
		    FROM UserLoanStatisticEntity u
		    LEFT JOIN UserViewEntity uv ON uv.id = u.userId
		    LEFT JOIN LoanViewEntity l
		        ON u.userId = l.userId
		       AND l.status = mentoring.acomi.loanservice.domain.model.LoanStatus.CONFIRMED
		       AND l.endDate < :today
		       AND l.returnedAt IS NULL
		    GROUP BY
		        u.userId,
		        u.overdueLoansCount,
		        u.totalDaysOverdue,
		        u.lastOverdueDate,
		        uv.cardNumber
		    ORDER BY
		        count(l.id) DESC,
		        u.overdueLoansCount DESC
		""")
	List<OverdueStatisticDto> getOverdueStatistics(@Param("today") LocalDate today);

}
