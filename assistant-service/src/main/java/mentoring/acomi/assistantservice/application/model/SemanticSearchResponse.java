package mentoring.acomi.assistantservice.application.model;

import java.util.List;

import mentoring.acomi.assistantservice.application.view.BookView;

public record SemanticSearchResponse(BookView source, List<RelatedReadBook> candidates) {

}
