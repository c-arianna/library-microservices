package mentoring.acomi.assistantservice.infrastracture.vectorstore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "assistant.vectorstore")
public record VectorStoreProperties(String url, String username, String password) {
}
