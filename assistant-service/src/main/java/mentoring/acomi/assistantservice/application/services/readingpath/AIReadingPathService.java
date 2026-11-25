package mentoring.acomi.assistantservice.application.services.readingpath;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import mentoring.acomi.assistantservice.application.context.ReadingPathContext;
import mentoring.acomi.assistantservice.application.model.ReadingPathSelection;
import mentoring.acomi.assistantservice.application.model.ReadingPathStep;
import mentoring.acomi.assistantservice.application.prompt.PromptFormatter;
import mentoring.acomi.assistantservice.application.view.BookView;

@Service
public class AIReadingPathService {

	private final ChatClient chatClient;
	private final Resource buildPrompt;
	private final Resource explainPrompt;
    private final PromptFormatter promptFormatter;
	
	public AIReadingPathService(ChatClient.Builder builder, @Value("${reading.build.prompt}") Resource buildPrompt,
			@Value("${reading.explain.prompt}") Resource explainPrompt, PromptFormatter promptFormatter) {
		this.chatClient = builder.build();
		this.buildPrompt = buildPrompt;
		this.explainPrompt = explainPrompt;
		this.promptFormatter = promptFormatter;
	}

	public ReadingPathSelection buildReadingPath(ReadingPathContext context) {

		return chatClient.prompt()
				.user(spec -> spec.text(buildPrompt)
						.param("candidateBooks", formatBookCandidates(context.candidateBooks())))
				.options(OllamaChatOptions.builder().numPredict(200))
				.call()
				.entity(ReadingPathSelection.class);
	}

	public String explainReadingPath(List<ReadingPathStep> path) {

		return chatClient.prompt()
				.user(spec -> spec.text(explainPrompt).param("readingPath", formatPath(path)))
				.options(OllamaChatOptions.builder().numPredict(200))
				.call()
				.content();
	}

	private String formatBookCandidates(Map<Integer, BookView> books) {
		return promptFormatter.formatBooks(books);
	}

	private String formatPath(List<ReadingPathStep> path) {
		return promptFormatter.formatPath(path);
	}
	
}