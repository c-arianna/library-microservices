package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.repositories.DailyLoanStatisticRepository;
import mentoring.acomi.loanservice.application.view.DailyLoanStatisticView;
import mentoring.acomi.sharedcorelibrary.eventstore.replay.ReplayProjection;
import mentoring.acomi.sharedjpalibrary.eventstore.replay.ReplayTableManager;

@Repository("replayDailyLoanStatisticRepo")
public class DailyLoanStatisticReplayRepository implements DailyLoanStatisticRepository, ReplayProjection {
	
	private static final String TABLE_MAIN = "daily_loan_statistics";
	private static final String TABLE_TMP = "daily_loan_statistics_tmp";
	
	private final JdbcTemplate jdbcTemplate;
	private final ReplayTableManager replayTableManager;
	
	public DailyLoanStatisticReplayRepository(JdbcTemplate jdbcTemplate, ReplayTableManager replayTableManager) {
		this.jdbcTemplate = jdbcTemplate;
		this.replayTableManager = replayTableManager;
	}

	@Override
	public void registerLoanCreated(LocalDate statisticsDate) {
		jdbcTemplate.update("""
				INSERT INTO %s(statistics_date, loans_created, loans_confirmed, loans_canceled, loans_returned) VALUES (?, 1, 0, 0, 0)
					ON DUPLICATE KEY UPDATE loans_created = loans_created + 1;
				""".formatted(TABLE_TMP), statisticsDate);
	}

	@Override
	public void registerLoanConfirmed(LocalDate statisticsDate) {
		jdbcTemplate.update("""
				INSERT INTO %s(statistics_date, loans_created, loans_confirmed, loans_canceled, loans_returned) VALUES (?, 0, 1, 0, 0)
					ON DUPLICATE KEY UPDATE loans_confirmed = loans_confirmed + 1;
				""".formatted(TABLE_TMP), statisticsDate);
	}

	@Override
	public void registerLoanReturned(LocalDate statisticsDate) {
		jdbcTemplate.update("""
				INSERT INTO %s(statistics_date, loans_created, loans_confirmed, loans_canceled, loans_returned) VALUES (?, 0, 0, 0, 1)
					ON DUPLICATE KEY UPDATE loans_returned = loans_returned + 1;
				""".formatted(TABLE_TMP), statisticsDate);
	}

	@Override
	public void registerLoanCanceled(LocalDate statisticsDate) {
		jdbcTemplate.update("""
				INSERT INTO %s(statistics_date, loans_created, loans_confirmed, loans_canceled, loans_returned) VALUES (?, 0, 0, 1, 0)
					ON DUPLICATE KEY UPDATE loans_canceled = loans_canceled + 1;
				""".formatted(TABLE_TMP), statisticsDate);
	}
	
	@Override
	public Optional<DailyLoanStatisticView> findByStatisticDate(LocalDate statisticsDate) {
		String query = """
		        SELECT
		            statistics_date,
		            loans_created,
		            loans_confirmed,
		            loans_canceled,
		            loans_returned
		        FROM %s
		        WHERE statistics_date = ?
		        """.formatted(TABLE_TMP);
	 
	 List<DailyLoanStatisticView> results = jdbcTemplate.query(query,
			 (rs, rowNum) -> new DailyLoanStatisticView(
	                    rs.getDate("statistics_date")!= null ? rs.getDate("statistics_date").toLocalDate() : null,
	                    rs.getInt("loans_created"),
	                    rs.getInt("loans_confirmed"),
	                    rs.getInt("loans_canceled"),
	                    rs.getInt("loans_returned")
	            ), statisticsDate
	    );
	 
	 return results.stream().findFirst();
	}
	
	@Override
	public void createTempTable() {
		replayTableManager.createTempTable(TABLE_MAIN, TABLE_TMP);
	}

	@Override
	public void swapTables() {
		replayTableManager.swapTables(TABLE_MAIN, TABLE_TMP);
	}

	@Override
	public void dropTempTable() {
		replayTableManager.dropTempTable(TABLE_TMP);		
	}

	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException("Not needed");		
	}

}
