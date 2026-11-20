package mentoring.acomi.assistantservice.infrastracture.config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MysqlDatasourceConfiguration {

    @Bean
    @Primary
    DataSource mysqlDataSource(MysqlDatasourceProperties properties) {
    	return DataSourceBuilder.create()
                .url(properties.url())
                .username(properties.username())
                .password(properties.password())
                .driverClassName(properties.driverClassName())
                .build();
    }
    
}