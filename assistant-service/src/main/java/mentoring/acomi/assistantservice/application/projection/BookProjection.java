package mentoring.acomi.assistantservice.application.projection;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.repositories.BookViewRepository;
import mentoring.acomi.assistantservice.application.view.BookView;

@Component
public class BookProjection extends AbstractProjectionOperations<BookView> {

	public BookProjection(BookViewRepository repository) {
        super(repository);
    }
}
