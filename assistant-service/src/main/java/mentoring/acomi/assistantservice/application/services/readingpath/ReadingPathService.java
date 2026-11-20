package mentoring.acomi.assistantservice.application.services.readingpath;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import mentoring.acomi.assistantservice.application.context.ReadingPathContext;
import mentoring.acomi.assistantservice.application.dto.ReadingPathRecommendation;
import mentoring.acomi.assistantservice.application.model.ReadingPathSelection;
import mentoring.acomi.assistantservice.application.model.ReadingPathStep;
import mentoring.acomi.assistantservice.application.view.BookView;

@Service
public class ReadingPathService {

	private final ReadingPathContextBuilder builder;
	private final AIReadingPathService aiService;

	public ReadingPathService(ReadingPathContextBuilder builder, AIReadingPathService aiService) {
		this.builder = builder;
		this.aiService = aiService;
	}

	public ReadingPathRecommendation getReadingPath(String isbn) {

		ReadingPathContext context = builder.build(isbn);

		ReadingPathSelection selection = aiService.buildReadingPath(context);

		List<ReadingPathStep> steps = buildReadingStep(context, selection);

		String explanation = aiService.explainReadingPath(steps);
		
		return new ReadingPathRecommendation(steps, explanation);
	}

	private List<ReadingPathStep> buildReadingStep(ReadingPathContext context, ReadingPathSelection selection) {
		
		Map<String, BookView> booksByIsbn = context.candidateBooks().stream()
				.collect(Collectors.toMap(BookView::isbn, Function.identity()));

		List<ReadingPathStep> steps = selection.books().stream().filter(bookPath -> booksByIsbn.containsKey(bookPath.isbn()))
				.map(bookPath -> {
					BookView view = booksByIsbn.get(bookPath.isbn());
					return new ReadingPathStep(view.isbn(), view.title(), view.author(), view.description(), bookPath.order());
				}).sorted(Comparator.comparing(ReadingPathStep::order)).toList();
		return steps;
	}

}