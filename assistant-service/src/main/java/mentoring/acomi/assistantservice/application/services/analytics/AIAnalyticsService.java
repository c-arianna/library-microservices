package mentoring.acomi.assistantservice.application.services.analytics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import mentoring.acomi.assistantservice.application.dto.AnalyticsQueryResponse;

@Service
public class AIAnalyticsService {
	
	private final Logger logger = LogManager.getLogger(AIAnalyticsService.class);
			
	private final Resource prompt;
	private final ChatClient chatClient;
	
	public AIAnalyticsService(@Value("${analytics.prompt}") Resource prompt, ChatClient.Builder builder) {
		this.prompt = prompt;
		this.chatClient = builder.build();		
	}
	
	public AnalyticsQueryResponse getAnalyticsData(String question) {
		
		logger.info("LLM calling, question: {}", question);
		
		AnalyticsQueryResponse response = chatClient.prompt()
				.user(spec -> spec.text(prompt).param("question", question))
				.call()
				.entity(AnalyticsQueryResponse.class);
		
		
		logger.info("LLM response: ");
		logger.info("Type: {}", response.dashboard());
		logger.info("Days: {}", response.lastDays());
		logger.info("Limit: {}", response.limit());
		logger.info("Explanation: {}", response.explanation());
		
		return response;
		
		
	}

}
