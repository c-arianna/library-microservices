package mentoring.acomi.assistantservice.application.services;

import java.util.List;

import org.springframework.stereotype.Service;

import mentoring.acomi.assistantservice.application.dto.BookRecommendation;
import mentoring.acomi.assistantservice.application.dto.ReadingPathRecommendation;
import mentoring.acomi.assistantservice.application.dto.RecommendationExplanation;
import mentoring.acomi.assistantservice.application.dto.SimilarBooksResult;
import mentoring.acomi.assistantservice.application.services.explanation.RecommendationExplanationService;
import mentoring.acomi.assistantservice.application.services.readingpath.ReadingPathService;
import mentoring.acomi.assistantservice.application.services.recommendation.RecommendationService;
import mentoring.acomi.assistantservice.application.services.semantic.SimilarBooksService;

@Service
public class AssistantService {
	
	private final RecommendationService recommendationService;
	private final SimilarBooksService similarBooksService;
	private final RecommendationExplanationService explainService;
	private final ReadingPathService readingPathService;
	
	public AssistantService(RecommendationService recommendationService, SimilarBooksService similarBooksService,
			RecommendationExplanationService explainService, ReadingPathService readingPathService) {
		this.recommendationService = recommendationService;
		this.similarBooksService = similarBooksService;
		this.explainService = explainService;
		this.readingPathService = readingPathService;
	}

	public List<BookRecommendation> recommend() {
		return recommendationService.recommend();
	}

	public SimilarBooksResult findSimilarBooks(String isbn) {
		return similarBooksService.findSimilarBooks(isbn);
	}

	public RecommendationExplanation explainBookRecommendation(String isbn) {
		return explainService.explainBookRecommendation(isbn);
	}

	public ReadingPathRecommendation getReadingPath(String isbn) {
		return readingPathService.getReadingPath(isbn);	
	}

}
