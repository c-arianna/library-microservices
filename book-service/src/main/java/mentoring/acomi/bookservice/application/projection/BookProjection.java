package mentoring.acomi.bookservice.application.projection;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.repositories.BookViewRepository;

@Component
public class BookProjection extends AbstractBookProjection {

    public BookProjection(BookViewRepository repository) {
        super(repository);
    }
}
