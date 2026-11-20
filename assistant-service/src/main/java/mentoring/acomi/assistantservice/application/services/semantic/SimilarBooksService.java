package mentoring.acomi.assistantservice.application.services.semantic;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import mentoring.acomi.assistantservice.application.context.SimilarBooksContext;
import mentoring.acomi.assistantservice.application.dto.SimilarBookResponse;
import mentoring.acomi.assistantservice.application.model.RelatedReadBook;
import mentoring.acomi.assistantservice.application.model.SemanticSearchResponse;
import mentoring.acomi.assistantservice.application.model.SimilarBook;
import mentoring.acomi.assistantservice.application.view.BookView;

@Service
public class SimilarBooksService {
	
    private final ChatClient chatClient;
    private final Resource prompt;
    private final SemanticSearchService semanticSearch;

    public SimilarBooksService(ChatClient.Builder builder, @Value("${searchSimilarBooks.prompt}") Resource prompt, 
    		SemanticSearchService semanticSearch) {
        this.chatClient = builder.build();
        this.prompt = prompt;
        this.semanticSearch = semanticSearch;
    }

    public List<SimilarBookResponse> findSimilarBooks(String isbn) {

        SemanticSearchResponse semanticSearchResponse = semanticSearch.findSimilarBookCandidates(isbn);
        
        SimilarBooksContext context = buildContext(semanticSearchResponse.source(), semanticSearchResponse.candidates());
    	
        List<SimilarBook> selected = getSimilarBooks(context);

        Map<String, BookView> candidatesByIsbn = context.candidateBooks().stream()
                                                    .collect(Collectors.toMap(BookView::isbn, Function.identity()));
       
        return selected.stream().filter(book -> candidatesByIsbn.containsKey(book.isbn()))
                				.map(book -> {
                						BookView view = candidatesByIsbn.get(book.isbn());

					                    return new SimilarBookResponse(
					                            view.isbn(),
					                            view.author(),
					                            view.title(),
					                            book.reason());
                					}).toList();
    }
    
    private SimilarBooksContext buildContext(BookView sourceBook, List<RelatedReadBook> documents) {
        List<BookView> candidateBooks = documents.stream().map(RelatedReadBook::book).toList();
        return new SimilarBooksContext(sourceBook, candidateBooks);
    }
    
    private List<SimilarBook> getSimilarBooks(SimilarBooksContext context) {

        return chatClient.prompt()
                         .user(userSpec -> userSpec.text(prompt)
	                         .param("sourceBook", formatBook(context.sourceBook()))
	                         .param("candidateBooks", formatBooks(context.candidateBooks())))
                         .call()
                         .entity(new ParameterizedTypeReference<List<SimilarBook>>() {});
    }
    
    private String formatBooks(List<BookView> books) {
		 return books.stream()
		            .map(book -> formatBook(book))
		            .collect(Collectors.joining("\n\n"));
	}

	private String formatBook(BookView book) {
		return """
				ISBN: %s
		        Titolo: %s
		        Autore: %s
		        Descrizione: %s
		        """.formatted(
		        		book.isbn(),
		                book.title(),
		                book.author(),
		                book.description());
	}
}
