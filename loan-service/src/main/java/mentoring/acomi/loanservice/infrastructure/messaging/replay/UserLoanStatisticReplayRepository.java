package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.repositories.UserLoanStatisticRepository;
import mentoring.acomi.loanservice.application.view.UserLoanStatisticView;
import mentoring.acomi.sharedcorelibrary.eventstore.replay.ReplayProjection;
import mentoring.acomi.sharedjpalibrary.eventstore.replay.ReplayTableManager;

@Repository("replayStatisticRepo")
public class UserLoanStatisticReplayRepository implements UserLoanStatisticRepository, ReplayProjection {

	private static final String TABLE_MAIN = "user_loan_statistics";
	private static final String TABLE_TMP = "user_loan_statistics_tmp";
	
	private final JdbcTemplate jdbcTemplate;
	private final ReplayTableManager replayTableManager;
	
	public UserLoanStatisticReplayRepository(JdbcTemplate jdbcTemplate, ReplayTableManager replayTableManager) {
		this.jdbcTemplate = jdbcTemplate;
		this.replayTableManager = replayTableManager;
	}

	@Override
	public void insert(UserLoanStatisticView view) {
		jdbcTemplate.update(
				"INSERT INTO %s(user_id, overdue_loans_count, total_days_overdue, last_overdue_date) VALUES (?,?,?,?)"
						.formatted(TABLE_TMP), view.userId(), view.overdueLoansCount(), view.totalDaysOverdue(), view.lastOverdueDate());
	}

	@Override
	public void statisticUpdate(String userId, long totalDaysOverdue, LocalDate lastOverdueDate) {
		jdbcTemplate.update("""
				      UPDATE %s 
				      SET overdue_loans_count = overdue_loans_count +1 ,
				          total_days_overdue = total_days_overdue + ?,
				          last_overdue_date =  
				              CASE
				               	WHEN last_overdue_date IS NULL OR last_overdue_date < ? THEN ?
				               	ELSE last_overdue_date
				           		END
					  WHERE user_id= ?
				      """.formatted(TABLE_TMP), totalDaysOverdue, lastOverdueDate, lastOverdueDate, userId);
	}

	@Override
	public Optional<UserLoanStatisticView> getUserLoanStatistic(String userId) {
		 String query = """
			        SELECT
			            user_id,
			            overdue_loans_count,
			            total_days_overdue,
			            last_overdue_date
			        FROM %s
			        WHERE user_id = ?
			        """.formatted(TABLE_TMP);
		 
		 List<UserLoanStatisticView> results = jdbcTemplate.query(query,
				 (rs, rowNum) -> new UserLoanStatisticView(
		                    rs.getString("user_id"),
		                    rs.getInt("overdue_loans_count"),
		                    rs.getLong("total_days_overdue"),
		                    rs.getDate("last_overdue_date") != null ? rs.getDate("last_overdue_date").toLocalDate() : null
		            ), userId
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
