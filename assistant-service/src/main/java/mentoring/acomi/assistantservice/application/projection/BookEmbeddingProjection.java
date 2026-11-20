package mentoring.acomi.assistantservice.application.projection;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.utility.HashUtils;
import mentoring.acomi.assistantservice.application.view.BookView;

@Component
public class BookEmbeddingProjection {

    private final VectorStore vectorStore;

    public BookEmbeddingProjection(VectorStore vectorStore) {
    	this.vectorStore = vectorStore;
    }
    
    public void index(BookView book) {

    	String content = buildContent(book);
        String hash = HashUtils.sha256(content);
          
        if (!alreadyIndexed(book.isbn(), hash)) {
            
			Document document = new Document(content,
	                        Map.of(
	                                "isbn", book.isbn(),
	                                "contentHash", hash,
	                                "title", book.title(),
	                                "author", book.author(),
	                                "indexedAt", Instant.now().toString()
	                        ));
	
	        vectorStore.add(List.of(document));
        
        }
    }
    
    private boolean alreadyIndexed(String isbn, String contentHash) {
    	SearchRequest request = SearchRequest.builder()
		        .filterExpression(String.format("isbn == '%s' AND contentHash == '%s'", isbn, contentHash))
		        .topK(1)
		        .build();
		List<Document> existing = vectorStore.similaritySearch(request);
    	return !existing.isEmpty();
	}

	private String buildContent(BookView book) {

        return """
                Titolo: %s

                Autore: %s

                Descrizione: %s
                """
                .formatted(
                        book.title(),
                        book.author(),
                        book.description());
    }
}