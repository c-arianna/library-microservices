package mentoring.acomi.assistantservice.application.dto;

import mentoring.acomi.assistantservice.application.model.DashboardType;

public record AnalyticsQueryResponse(DashboardType dashboard, Integer lastDays, Integer limit, String explanation) {
	
}