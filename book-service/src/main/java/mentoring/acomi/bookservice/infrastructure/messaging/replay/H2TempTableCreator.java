package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedlibrary.replay.TempTableCreator;

@Component
@Profile("H2")
public class H2TempTableCreator implements TempTableCreator {

    private final JdbcTemplate jdbcTemplate;

    public H2TempTableCreator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void createTempTable(String tableTmp, String tableMain) {
        
    	jdbcTemplate.execute("""
			    DROP TABLE IF EXISTS %s
			""".formatted(tableTmp));

        jdbcTemplate.execute("""
        						CREATE TABLE %s (
								  isbn varchar(17) not null,
								  author varchar(100) not null,
								  title varchar(100) not null,
								  description varchar(500),
								  total_copies integer not null default 0,
								  borrowed_copies integer not null default 0,
								  reserved_copies integer not null default 0,
								  created_at timestamp not null default current_timestamp,
								  updated_at timestamp not null default current_timestamp,
								  primary key (isbn))
						""".formatted(tableTmp));
    }

}
