package mentoring.acomi.assistantservice.application.services.explanation;

import org.springframework.stereotype.Service;

import mentoring.acomi.assistantservice.application.context.ExplanationContext;
import mentoring.acomi.assistantservice.application.dto.RecommendationExplanation;
import mentoring.acomi.assistantservice.application.security.UserIdentityProvider;
import mentoring.acomi.assistantservice.application.services.recommendation.AIExplanationRecommendationService;

@Service
public class RecommendationExplanationService {
	
    private final RecommendationExplanationContextBuilder explanationBuilder;
    private final AIExplanationRecommendationService aiService;
    private final UserIdentityProvider identityProvider;
    
    public RecommendationExplanationService(RecommendationExplanationContextBuilder explanationBuilder, 
    		AIExplanationRecommendationService aiService, UserIdentityProvider identityProvider) {
    	this.aiService = aiService;
    	this.explanationBuilder = explanationBuilder;
    	this.identityProvider = identityProvider;
    }
        
    public RecommendationExplanation explainBookRecommendation(String isbn) {

        ExplanationContext context = explanationBuilder.buildExplanationContext(identityProvider.getUserIdentityProviderId(), isbn);

        String explanation = aiService.explainRecommendation(context);

        return new RecommendationExplanation(isbn, explanation);
    }
    
}
