package mentoring.acomi.userservice.infrastructure.messaging.replay;

import java.time.Instant;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import mentoring.acomi.sharedlibrary.eventstore.replay.BaseReplayRepository;
import mentoring.acomi.sharedlibrary.model.UserStatus;
import mentoring.acomi.sharedlibrary.replay.TempTableCreator;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.view.UserView;

@Repository("replayRepo")
public class UserViewReplayRepository extends BaseReplayRepository implements UserViewRepository {

	private static final String TABLE_MAIN = "user_view";
	private static final String TABLE_TMP = "user_view_tmp";

	private final TempTableCreator tableCreator;
	
	public UserViewReplayRepository(JdbcTemplate jdbcTemplate, TempTableCreator tableCreator) {
		super(jdbcTemplate, TABLE_MAIN, TABLE_TMP);
		this.tableCreator = tableCreator;
	}

	public void createTempTable() {
		tableCreator.createTempTable(TABLE_TMP, TABLE_MAIN);
	}

	@Override
	public void add(UserView user, Instant createdAt) {
		jdbcTemplate.update("INSERT INTO %s(id, email, name, lastname, user_identity_provider_id, role, status, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?)"
				.formatted(TABLE_TMP), user.id(), user.email(), user.name(), user.lastname(), user.userIdentityProviderId(), user.role().name(), 
				      user.status().name(), createdAt, createdAt);
	}

	@Override
	public void updateStatus(String id, UserStatus status, Instant updatedAt) {
		jdbcTemplate.update("UPDATE %s SET status = ?, updated_at = ? WHERE id = ?".formatted(TABLE_TMP), status.name(), updatedAt, id);
	}

}
