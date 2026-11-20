package mentoring.acomi.assistantservice.application.dto;

import java.util.List;

import mentoring.acomi.assistantservice.application.model.ReadingPathStep;

public record ReadingPathRecommendation(List<ReadingPathStep> books, String explanation) {

}