package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.infrastructure.persistence.entity.DailyLoanStatisticEntity;

public interface DailyLoanStatisticJpaRepository extends JpaRepository<DailyLoanStatisticEntity, LocalDate> {
	@Modifying
	@Transactional
	@NativeQuery("""
			 INSERT INTO daily_loan_statistics (
                statistics_date,
                loans_created,
                loans_confirmed,
                loans_canceled,
                loans_returned
            )
            VALUES (
                :id,
                1,
                0,
                0,
                0
            )
            ON DUPLICATE KEY UPDATE
                loans_created = loans_created + 1
			""")
	void registerLoanCreated(@Param("id") LocalDate statisticsDate);
	
	@Modifying
	@Transactional
	@NativeQuery("""
			 INSERT INTO daily_loan_statistics (
               statistics_date,
               loans_created,
               loans_confirmed,
               loans_canceled,
               loans_returned
           )
           VALUES (
               :id,
               0,
               1,
               0,
               0
           )
           ON DUPLICATE KEY UPDATE
               loans_confirmed = loans_confirmed + 1
			""")
	void registerLoanConfirmed(@Param("id") LocalDate statisticsDate);
	
	@Modifying
	@Transactional
	@NativeQuery("""
			 INSERT INTO daily_loan_statistics (
            statistics_date,
            loans_created,
            loans_confirmed,
            loans_canceled,
            loans_returned
	        )
	        VALUES (
	            :id,
	            0,
	            0,
	            0,
	            1
	        )
	        ON DUPLICATE KEY UPDATE
	            loans_returned = loans_returned + 1
			""")
	void registerLoanReturned(@Param("id") LocalDate statisticsDate);
	
	@Modifying
	@Transactional
	@NativeQuery("""
			 INSERT INTO daily_loan_statistics (
           statistics_date,
           loans_created,
           loans_confirmed,
           loans_canceled,
           loans_returned
	        )
	        VALUES (
	            :id,
	            0,
	            0,
	            1,
	            0
	        )
	        ON DUPLICATE KEY UPDATE
	            loans_canceled = loans_canceled + 1
			""")
	void registerLoanCanceled(@Param("id") LocalDate statisticsDate);

	@Query("SELECT d FROM DailyLoanStatisticEntity d WHERE d.statisticsDate BETWEEN :from AND :to ORDER BY d.statisticsDate")
	List<DailyLoanStatisticEntity> findStatistics(LocalDate from, LocalDate to);

}
