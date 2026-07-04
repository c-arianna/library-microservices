package mentoring.acomi.userservice.infrastructure.messaging.replay;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.replay.TempTableCreator;

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
							    id varchar(36) not null,
								email varchar(100) not null,
								name varchar(50) not null,
								lastname varchar(50) not null,
								user_identity_provider_id varchar(100) not null,
								role varchar(20) not null default 'READER',
								status varchar(20) not null default 'ACTIVE',
								created_at timestamp not null default current_timestamp,
								updated_at timestamp not null default current_timestamp,
								unique (email),
								primary key(id))""".formatted(tableTmp));
    }

}
