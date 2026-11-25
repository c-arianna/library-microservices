package mentoring.acomi.assistantservice.application.services.recommendation;

import java.util.List;

import org.springframework.stereotype.Service;

import mentoring.acomi.assistantservice.application.context.RecommendationContext;
import mentoring.acomi.assistantservice.application.dto.BookRecommendation;
import mentoring.acomi.assistantservice.application.dto.BookRecommendationsResult;
import mentoring.acomi.assistantservice.application.model.RecommendationType;
import mentoring.acomi.assistantservice.application.security.UserIdentityProvider;
import mentoring.acomi.assistantservice.application.view.BookView;

@Service
public class RecommendationService {
	
    private final RecommendationContextBuilder builder;
    private final UserIdentityProvider identityProvider;

    public RecommendationService(RecommendationContextBuilder builder, UserIdentityProvider identityProvider) {
    	this.builder = builder;
    	this.identityProvider = identityProvider;
    }
    
    public BookRecommendationsResult recommend() {
        
    	RecommendationContext context = builder.buildRecommendationContext(identityProvider.getUserIdentityProviderId());
            	
    	List<BookRecommendation> bookRecommendation = recommendBooks(context.recommendedBooks());
    	
        return context.readBooks().isEmpty() ? new BookRecommendationsResult(RecommendationType.POPULAR, bookRecommendation) :
        	                                   new BookRecommendationsResult(RecommendationType.PERSONALIZED, bookRecommendation);
    }
        
    private List<BookRecommendation> recommendBooks(List<BookView> books) {
    	return books.stream().map(b -> new BookRecommendation(b.isbn(), b.author(), b.title())).toList();
	}

}
