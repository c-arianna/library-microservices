package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.replay.TempTableCreator;

@Component
@Profile("!H2")
public class DefaultTempTableCreator implements TempTableCreator {

	private final JdbcTemplate jdbcTemplate;

	public DefaultTempTableCreator(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void createTempTable(String tableTmp, String tableMain) {
		jdbcTemplate.execute("DROP TABLE IF EXISTS %s".formatted(tableTmp));
		jdbcTemplate.execute("CREATE TABLE %s LIKE %s".formatted(tableTmp, tableMain));
	}

}