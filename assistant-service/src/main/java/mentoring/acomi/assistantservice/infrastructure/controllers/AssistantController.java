package mentoring.acomi.assistantservice.infrastructure.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import mentoring.acomi.assistantservice.application.dto.BookRecommendationsResult;
import mentoring.acomi.assistantservice.application.dto.ReadingPathRecommendation;
import mentoring.acomi.assistantservice.application.dto.RecommendationExplanation;
import mentoring.acomi.assistantservice.application.dto.SimilarBooksResult;
import mentoring.acomi.assistantservice.application.services.AssistantService;

@RestController
public class AssistantController {

	private final AssistantService service;
		
	public AssistantController(AssistantService service) {
		this.service = service;
	}
	
	@PreAuthorize("hasAnyRole('READER')")
	@GetMapping("/recommendation")
	public BookRecommendationsResult getBookRecommendations(){
		return service.recommend();
	}
	
	@GetMapping("/books/{isbn}/similar")
	public SimilarBooksResult findSimilarBooks(@PathVariable String isbn) {
	    return service.findSimilarBooks(isbn);
	}
	
	@PreAuthorize("hasAnyRole('READER')")
	@GetMapping("/recommendations/explain/{isbn}")
	public RecommendationExplanation explainBookRecommendation(@PathVariable String isbn) {
	    return service.explainBookRecommendation(isbn);
	}
	
	@GetMapping("/books/{isbn}/readingpath")
	public ReadingPathRecommendation getReadingPath(@PathVariable String isbn) {
		return service.getReadingPath(isbn);
	}
}
