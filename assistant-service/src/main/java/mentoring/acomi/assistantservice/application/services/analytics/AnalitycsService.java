package mentoring.acomi.assistantservice.application.services.analytics;

import java.util.Optional;

import org.springframework.stereotype.Service;

import mentoring.acomi.assistantservice.application.dto.AnalyticsQueryResponse;
import mentoring.acomi.assistantservice.application.model.DashboardType;

@Service
public class AnalitycsService {

	private AIAnalyticsService aiService;
	private AnalyticsIntentRouter router;

	public AnalitycsService(AIAnalyticsService aiService, AnalyticsIntentRouter router) {
		this.aiService = aiService;
		this.router = router;
	}

	public AnalyticsQueryResponse loadAnalyticsData(String question) {

		Optional<DashboardType> routing = router.resolveDashboardType(question);

		if (routing.isPresent() && routing.get() == DashboardType.UNSUPPORTED) {
			return new AnalyticsQueryResponse(DashboardType.UNSUPPORTED, null, null,
					"Analisi non supportata dalle dashboard disponibili.");
		}

		AnalyticsQueryResponse llmResponse = aiService.getAnalyticsData(question);

		if (llmResponse.dashboard() == DashboardType.UNSUPPORTED) {
			return new AnalyticsQueryResponse(DashboardType.UNSUPPORTED, null, null, "Analisi non supportata dalle dashboard disponibili.");
		}

		return routing.isEmpty() ? llmResponse
				: new AnalyticsQueryResponse(routing.get(), llmResponse.lastDays(), llmResponse.limit(),
						llmResponse.explanation());

	}
}
