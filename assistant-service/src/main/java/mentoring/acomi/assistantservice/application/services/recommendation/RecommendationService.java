package mentoring.acomi.assistantservice.application.services.recommendation;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import mentoring.acomi.assistantservice.application.context.RecommendationContext;
import mentoring.acomi.assistantservice.application.dto.BookRecommendation;
import mentoring.acomi.assistantservice.application.model.BookRecommendationSelection;
import mentoring.acomi.assistantservice.application.security.UserIdentityProvider;
import mentoring.acomi.assistantservice.application.services.explanation.AIRecommendationService;
import mentoring.acomi.assistantservice.application.view.BookView;

@Service
public class RecommendationService {

	private final Logger logger = LogManager.getLogger(RecommendationService.class);
	
    private final RecommendationContextBuilder builder;
    private final AIRecommendationService aiService;
    private final UserIdentityProvider identityProvider;

    public RecommendationService(RecommendationContextBuilder builder,AIRecommendationService aiService,
    		UserIdentityProvider identityProvider) {
    	this.builder = builder; 
    	this.aiService = aiService;
    	this.identityProvider = identityProvider;
    }
    
    public List<BookRecommendation> recommend() {
        RecommendationContext context = builder.buildRecommendationContext(identityProvider.getUserIdentityProviderId());
                
        Map<String, BookView> candidatesByIsbn = context.candidates().stream()
        		                       .collect(Collectors.toMap(BookView::isbn, Function.identity()));
        Set<String> readBooks = context.readBooks().stream().map(BookView::isbn).collect(Collectors.toSet());
        
        if(context.readBooks().isEmpty()) {
        	return recommendForNewReader(context, candidatesByIsbn);
        }
        
        logger.info("Calling AI Recommendation");
        
        List<BookRecommendationSelection> selections = aiService.recommend(context);
        
        selections.stream().forEach(s -> logger.info("AI recommend ISBN: {}", s.isbn()));
        return getBookRecommendation(candidatesByIsbn, readBooks, selections);
    }
        
    private List<BookRecommendation> recommendForNewReader(RecommendationContext context,  Map<String, BookView> candidatesByIsbn) {
    	List<BookRecommendationSelection> selections = aiService.recommendForNewReader(context); 
    	return getBookRecommendation(candidatesByIsbn, Set.of(), selections);
	}

	private List<BookRecommendation> getBookRecommendation(Map<String, BookView> candidates, Set<String> readBooks, 
    		List<BookRecommendationSelection> selections) {
		
    	Set<String> seen = new HashSet<>();
    	
    	return selections.stream().filter(selection -> candidates.containsKey(selection.isbn()))
    			                  .filter(selection -> seen.add(selection.isbn()))
    			                  .filter(selection -> !readBooks.contains(selection.isbn())).map(selection -> {
    			                	  	BookView book = candidates.get(selection.isbn());
    			                	  	return new BookRecommendation(book.isbn(), book.author(), book.title());
    	        }).toList();
    	
	}

}
