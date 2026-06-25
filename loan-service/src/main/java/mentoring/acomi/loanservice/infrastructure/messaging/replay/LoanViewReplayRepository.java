package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.time.Instant;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.sharedlibrary.eventstore.replay.BaseReplayRepository;
import mentoring.acomi.sharedlibrary.replay.TempTableCreator;

@Repository("replayRepo")
public class LoanViewReplayRepository extends BaseReplayRepository implements LoanViewRepository{
	
	private static final String TABLE_MAIN = "loan_view";
	private static final String TABLE_TMP = "loan_view_tmp";

	private final TempTableCreator tableCreator;
	
	public LoanViewReplayRepository(JdbcTemplate jdbcTemplate, TempTableCreator creator) {
		super(jdbcTemplate, TABLE_MAIN, TABLE_TMP);
		this.tableCreator = creator;
	}

	public void createTempTable() {
		tableCreator.createTempTable(TABLE_TMP, TABLE_MAIN);
	}

	@Override
	public void insertRequest(LoanView loan, Instant createdAt) {
		jdbcTemplate.update("INSERT INTO %s(id, isbn, user_id, start_date, end_date, status, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?)".formatted(TABLE_TMP),
				loan.id(), loan.isbn(), loan.userId(), loan.start(), loan.end(), loan.status().name(), createdAt, createdAt);
	}

	@Override
	public void updateStatus(String id, LoanStatus status, Instant updatedAt) {
		jdbcTemplate.update("UPDATE %s SET status = ?, updated_at = ? WHERE id = ?".formatted(TABLE_TMP), status.name(), updatedAt, id);
	}

}
