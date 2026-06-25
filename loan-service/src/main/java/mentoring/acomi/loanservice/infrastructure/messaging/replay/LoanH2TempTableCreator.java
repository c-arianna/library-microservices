package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("H2")
public class LoanH2TempTableCreator {

	private final JdbcTemplate jdbcTemplate;

	public LoanH2TempTableCreator(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void createTempTableInternal(String tableTmp) {
		jdbcTemplate.execute("""
				    DROP TABLE IF EXISTS %s
				""".formatted(tableTmp));

		jdbcTemplate.execute("""
				   			CREATE TABLE %s (
				                        id varchar(36) not null,
				                        isbn varchar(17) not null,
				                        user_id varchar(36) not null,
				                        start_date date not null,
				                        end_date date not null,
				                        status varchar(20) not null default 'PENDING',
				                        created_at timestamp not null default current_timestamp,
				                        updated_at timestamp not null default current_timestamp,
				                        primary key(id)
				)""".formatted(tableTmp));
	}

}
