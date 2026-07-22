package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.time.Instant;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.sharedcodelibrary.eventstore.replay.ReplayProjection;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.sharedjpalibrary.eventstore.replay.ReplayTableManager;


@Repository("userReplayRepo")
public class UserViewReplayRepository implements UserViewRepository, ReplayProjection {

	private static final String TABLE_MAIN = "user_view";
	private static final String TABLE_TMP = "user_view_tmp";
	
	private final JdbcTemplate jdbcTemplate;
	private final ReplayTableManager replayTableManager;
	
	public UserViewReplayRepository(JdbcTemplate jdbcTemplate, ReplayTableManager replayTableManager) {
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
	public void add(UserView user, Instant createdAt) {
		jdbcTemplate.update("INSERT INTO %s(id, email, name, lastname, card_number, status, user_identity_provider_id, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?)"
				.formatted(TABLE_TMP), user.id(), user.email(), user.name(), user.lastname(), user.cardNumber(), user.status().name(), 
				user.identityProviderId(), createdAt, createdAt);
	}

	@Override
	public void updateStatus(String id, UserStatus status, Instant updatedAt) {
		jdbcTemplate.update("UPDATE %s SET status = ?, updated_at = ? WHERE id = ?".formatted(TABLE_TMP), status.name(), updatedAt, id);
	}

	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException("Not needed");
	}
	
	@Override
	public void updateCardNumber(String id, String cardNumber, Instant updatedAt) {
		jdbcTemplate.update("UPDATE %s SET card_number = ?, updated_at = ? WHERE id = ?".formatted(TABLE_TMP), cardNumber, updatedAt, id);
	}

}
