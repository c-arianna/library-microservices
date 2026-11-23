package mentoring.acomi.assistantservice.application.dto;

import java.util.List;

public record SimilarBooksResult(BookDetail sourceBook, List<SimilarBookResponse> books) {
}