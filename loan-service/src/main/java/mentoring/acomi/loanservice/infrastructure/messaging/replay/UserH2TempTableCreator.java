package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("H2")
public class UserH2TempTableCreator {

    private final JdbcTemplate jdbcTemplate;

    public UserH2TempTableCreator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createTempTableInternal(String tableTmp) {
        
    	jdbcTemplate.execute("""
			    DROP TABLE IF EXISTS %s
			""".formatted(tableTmp));

	    jdbcTemplate.execute("""
			                CREATE TABLE %s (
							    id varchar(36) not null,
								email varchar(100) not null,
								status varchar(20) not null default 'ACTIVE',
								created_at timestamp not null default current_timestamp,
								updated_at timestamp not null default current_timestamp,
								unique (email),
								primary key(id))""".formatted(tableTmp));
    }

}
