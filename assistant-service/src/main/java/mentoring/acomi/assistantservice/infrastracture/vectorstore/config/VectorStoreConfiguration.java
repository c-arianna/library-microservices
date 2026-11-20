package mentoring.acomi.assistantservice.infrastracture.vectorstore.config;

import javax.sql.DataSource;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableConfigurationProperties(VectorStoreProperties.class)
@ConditionalOnProperty(
	    name = "assistant.vectorstore.enabled",
	    havingValue = "true"
	)
public class VectorStoreConfiguration {

	@Bean(name = "vectorDataSource")
    DataSource vectorDataSource(VectorStoreProperties properties) {
		
        return DataSourceBuilder.create()
                .url(properties.url())
                .username(properties.username())
                .password(properties.password())
                .build();
    }
    
	@Bean(name = "vectorJdbcTemplate")
    JdbcTemplate vectorJdbcTemplate(@Qualifier("vectorDataSource") DataSource vectorDataSource) {
        return new JdbcTemplate(vectorDataSource);
    }
    
    @Bean
    PgVectorStore vectorStore(@Qualifier("vectorJdbcTemplate") JdbcTemplate vectorJdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(
                vectorJdbcTemplate,
                embeddingModel)
        		.initializeSchema(true)
                .build();
    }
   
}
