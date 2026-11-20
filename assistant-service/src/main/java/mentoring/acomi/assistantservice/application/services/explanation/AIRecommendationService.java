package mentoring.acomi.assistantservice.application.services.explanation;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import mentoring.acomi.assistantservice.application.context.RecommendationContext;
import mentoring.acomi.assistantservice.application.model.BookRecommendationSelection;
import mentoring.acomi.assistantservice.application.prompt.PromptFormatter;
import mentoring.acomi.assistantservice.application.view.BookView;

@Service
public class AIRecommendationService {

	private final Logger logger = LogManager.getLogger(AIRecommendationService.class);

	private final Resource recommendationPrompt;
	private final ChatClient chatClient;
	private final Resource recommendationForNewReaderPrompt;
	private final PromptFormatter promptFormatter;

	public AIRecommendationService(ChatClient.Builder builder, @Value("${recommendation.prompt}") Resource recommendationPrompt, 
			@Value("${recommendation.newreader.prompt}") Resource recommendationForNewReaderPrompt, 
			PromptFormatter promptFormatter) {
		this.chatClient = builder.build();
		this.recommendationPrompt = recommendationPrompt;
		this.recommendationForNewReaderPrompt = recommendationForNewReaderPrompt;
		this.promptFormatter = promptFormatter;
	}

	public List<BookRecommendationSelection> recommend(RecommendationContext context) {

		logger.info("Calling Ollama");
		return chatClient.prompt().user(userSpec -> userSpec.text(recommendationPrompt)
												            .param("history", formatBooks(context.readBooks()))
			                                                .param("candidateBooks", formatBooks(context.candidates())))
			                      .call()
			                      .entity(new ParameterizedTypeReference<List<BookRecommendationSelection>>() {
		});
	}

	public List<BookRecommendationSelection> recommendForNewReader(RecommendationContext context) {

		logger.info("Calling Ollama");
		return chatClient.prompt().user(userSpec -> userSpec.text(recommendationForNewReaderPrompt)
			                                                .param("popularBooks", formatBooks(context.candidates())))
			                      .call()
			                      .entity(new ParameterizedTypeReference<List<BookRecommendationSelection>>() {
		});
	}
	
	private String formatBooks(List<BookView> books) {
		return promptFormatter.formatBooks(books);
	}

}