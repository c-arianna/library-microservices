package mentoring.acomi.assistantservice.application.services.recommendation;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import mentoring.acomi.assistantservice.application.context.ExplanationContext;
import mentoring.acomi.assistantservice.application.model.RelatedReadBook;
import mentoring.acomi.assistantservice.application.prompt.PromptFormatter;
import mentoring.acomi.assistantservice.application.view.BookView;

@Service
public class AIExplanationRecommendationService {

	private final Resource explainRecommendationPrompt;
	private final ChatClient chatClient;
	private final PromptFormatter promptFormatter;

	public AIExplanationRecommendationService(ChatClient.Builder builder, 
			@Value("${recommendation.explain.prompt}") Resource explainRecommendationPrompt, PromptFormatter promptFormatter) {
		this.chatClient = builder.build();
		this.explainRecommendationPrompt = explainRecommendationPrompt;
		this.promptFormatter = promptFormatter;
	}

	public String explainRecommendation(ExplanationContext context) {
		return chatClient.prompt().user(spec -> spec.text(explainRecommendationPrompt)
													.param("relatedReadBooks", formatRelatedBooks(context.relatedReadBooks()))
													.param("recommendedBook", formatBook(context.recommendedBook())))
				                  .call()
				                  .content();
	}

	private String formatRelatedBooks(List<RelatedReadBook> relatedReadBooks) {
		return promptFormatter.formatRelatedBooks(relatedReadBooks);
	}
	
	private String formatBook(BookView book) {
		return promptFormatter.formatBook(book);
	}

}