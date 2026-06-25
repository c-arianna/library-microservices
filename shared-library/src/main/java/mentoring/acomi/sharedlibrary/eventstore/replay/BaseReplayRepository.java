package mentoring.acomi.sharedlibrary.eventstore.replay;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class BaseReplayRepository {

	protected JdbcTemplate jdbcTemplate;
	private final String table;
	private final String tableTmp;
	
	public BaseReplayRepository(JdbcTemplate jdbcTemplate, String table, String tableTmp) {
		this.jdbcTemplate = jdbcTemplate;
		this.table = table;
		this.tableTmp = tableTmp;
	}
	
	@Transactional
	public void swapTables() {

		jdbcTemplate.execute("ALTER TABLE %s RENAME TO %s_old".formatted(table, table));
		jdbcTemplate.execute("ALTER TABLE %s RENAME TO %s".formatted(tableTmp, table));
		jdbcTemplate.execute("DROP TABLE %s_old".formatted(table));
	}

	public void dropTempTable() {
		jdbcTemplate.execute("DROP TABLE IF EXISTS %s".formatted(tableTmp));
	}

}
