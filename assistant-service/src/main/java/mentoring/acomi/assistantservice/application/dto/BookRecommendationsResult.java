package mentoring.acomi.assistantservice.application.dto;

import java.util.List;

import mentoring.acomi.assistantservice.application.model.RecommendationType;

public record BookRecommendationsResult(RecommendationType type, List<BookRecommendation> books) {

}
