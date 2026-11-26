package mentoring.acomi.assistantservice.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AnalyticsQueryRequest(@NotBlank(message = "question required") String question) {

}
