package mentoring.acomi.assistantservice.infrastructure.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mentoring.acomi.assistantservice.application.dto.AnalyticsQueryRequest;
import mentoring.acomi.assistantservice.application.dto.AnalyticsQueryResponse;
import mentoring.acomi.assistantservice.application.services.analytics.AnalitycsService;

@RestController
public class AnalyticsController {
	
	private final AnalitycsService service;
	
	public AnalyticsController(AnalitycsService service) {
		this.service = service;
	}
	
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	@PostMapping("/analytics/query")
	public AnalyticsQueryResponse loadAnalyticsData(@RequestBody @Valid AnalyticsQueryRequest request) {
		return service.loadAnalyticsData(request.question());
	}
}
