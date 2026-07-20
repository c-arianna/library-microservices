package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.time.Instant;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.sharedcodelibrary.eventstore.replay.ReplayProjection;
import mentoring.acomi.sharedjpalibrary.eventstore.replay.ReplayTableManager;

@Repository("replayRepo")
public class LoanViewReplayRepository implements LoanViewRepository, ReplayProjection {

	private static final String TABLE_MAIN = "loan_view";
	private static final String TABLE_TMP = "loan_view_tmp";
	
	private final JdbcTemplate jdbcTemplate;
	private final ReplayTableManager replayTableManager;

	public LoanViewReplayRepository(JdbcTemplate jdbcTemplate, ReplayTableManager replayTableManager) {
		this.jdbcTemplate = jdbcTemplate;
		this.replayTableManager = replayTableManager;
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
	public void insertRequest(LoanView loan, Instant createdAt) {
		jdbcTemplate.update(
				"INSERT INTO %s(id, isbn, user_id, start_date, end_date, status, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?)"
						.formatted(TABLE_TMP), loan.id(), loan.isbn(), loan.userId(), loan.start(), loan.end(), 
						                       loan.status().name(), createdAt, createdAt);
	}

	@Override
	public void updateStatus(String id, LoanStatus status, Instant updatedAt) {
		jdbcTemplate.update("UPDATE %s SET status = ?, updated_at = ? WHERE id = ?".formatted(TABLE_TMP), status.name(), updatedAt, id);
	}

	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException("Not needed");
	}

}
