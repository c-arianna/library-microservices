package mentoring.acomi.assistantservice.infrastructure.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import mentoring.acomi.assistantservice.application.dto.BookRecommendation;
import mentoring.acomi.assistantservice.application.dto.ReadingPathRecommendation;
import mentoring.acomi.assistantservice.application.dto.RecommendationExplanation;
import mentoring.acomi.assistantservice.application.dto.SimilarBookResponse;
import mentoring.acomi.assistantservice.application.services.AssistantService;

@RestController
public class AssistantController {

	private final AssistantService service;
		
	public AssistantController(AssistantService service) {
		this.service = service;
	}
	
	@GetMapping("/recommendation")
	public List<BookRecommendation> getBookRecommendations(){
		return service.recommend();
	}
	
	@GetMapping("/books/{isbn}/similar")
	public List<SimilarBookResponse> findSimilarBooks(@PathVariable String isbn) {
	    return service.findSimilarBooks(isbn);
	}
	
	@GetMapping("/recommendations/explain/{isbn}")
	public RecommendationExplanation explainBookRecommendation(@PathVariable String isbn) {
	    return service.explainBookRecommendation(isbn);
	}
	
	@GetMapping("/books/{isbn}/readingpath")
	public ReadingPathRecommendation getReadingPath(@PathVariable String isbn) {
		return service.getReadingPath(isbn);
	}
}
