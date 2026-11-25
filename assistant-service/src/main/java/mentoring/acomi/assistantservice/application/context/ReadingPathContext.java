package mentoring.acomi.assistantservice.application.context;

import java.util.Map;

import mentoring.acomi.assistantservice.application.view.BookView;

public record ReadingPathContext(BookView sourceBook, Map<Integer, BookView> candidateBooks) {

}