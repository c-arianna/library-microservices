package mentoring.acomi.assistantservice.infrastracture.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
public record MysqlDatasourceProperties(String url, String username, String password, String driverClassName) {
}
