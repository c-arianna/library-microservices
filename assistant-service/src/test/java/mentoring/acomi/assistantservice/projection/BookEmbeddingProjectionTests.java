package mentoring.acomi.assistantservice.projection;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import mentoring.acomi.assistantservice.application.projection.BookEmbeddingProjection;
import mentoring.acomi.assistantservice.application.view.BookView;
import mentoring.acomi.assistantservice.infrastracture.vectorstore.config.VectorStoreConfiguration;

@SpringBootTest(
	    classes = {
	        VectorStoreConfiguration.class,
	        BookEmbeddingProjection.class
	    }
	)
@Testcontainers
public class BookEmbeddingProjectionTests {

    @Autowired
    private BookEmbeddingProjection projection;

    @Autowired
    private VectorStore vectorStore;
    
    @MockitoBean
    private EmbeddingModel embeddingModel;

    @SuppressWarnings("resource")
	@Container
    static PostgreSQLContainer<?> postgres =  new PostgreSQLContainer<>(DockerImageName.parse("pgvector/pgvector:pg17"))
                                                                                       .withInitScript("pgvector-init.sql");
    
    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("assistant.vectorstore.url", postgres::getJdbcUrl);
        registry.add("assistant.vectorstore.username", postgres::getUsername);
        registry.add("assistant.vectorstore.password", postgres::getPassword);
    }
    
    @BeforeEach
    void setup() {
    	float[] vector = new float[1536];
        Arrays.fill(vector, 0.1f);
        
        when(embeddingModel.embed(anyList(), any(), any())).thenReturn(List.of(vector));
        when(embeddingModel.embed(anyString())).thenReturn(vector);
    }
    
    @Test
    void shouldGenerateEmbedding() {

        BookView book = new BookView("9788804336327", "Italo Calvino", "Il barone rampante", "", 0, 0, 0);

        projection.index(book);

        List<Document> result =
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(book.title())
                                .topK(10)
                                .similarityThreshold(0.0)
                                .build());

        assertFalse(result.isEmpty());
    }
}