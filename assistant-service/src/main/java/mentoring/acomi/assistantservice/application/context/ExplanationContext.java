package mentoring.acomi.assistantservice.application.context;

import java.util.List;

import mentoring.acomi.assistantservice.application.model.RelatedReadBook;
import mentoring.acomi.assistantservice.application.view.BookView;

public record ExplanationContext(BookView recommendedBook, List<RelatedReadBook> relatedReadBooks) {

}
