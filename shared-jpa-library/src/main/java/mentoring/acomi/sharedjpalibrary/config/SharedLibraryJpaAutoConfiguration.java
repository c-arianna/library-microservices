package mentoring.acomi.sharedjpalibrary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import mentoring.acomi.sharedjpalibrary.eventstore.replay.ReplayTableManager;

@Configuration
public class SharedLibraryJpaAutoConfiguration {

    @Bean
    ReplayTableManager replayTableManager(JdbcTemplate jdbcTemplate) {
        return new ReplayTableManager(jdbcTemplate);
    }
    
}