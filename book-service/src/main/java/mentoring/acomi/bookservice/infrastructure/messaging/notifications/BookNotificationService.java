package mentoring.acomi.bookservice.infrastructure.messaging.notifications;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.errors.BookNotFound;
import mentoring.acomi.bookservice.application.repositories.BookViewQueryRepository;
import mentoring.acomi.bookservice.application.view.BookView;

@Component
public class BookNotificationService {

    private final BookViewQueryRepository queryRepository;
    private final BookNotificationPublisher publisher;
    
    public BookNotificationService(BookViewQueryRepository queryRepository, BookNotificationPublisher publisher) {
		this.queryRepository = queryRepository;
		this.publisher = publisher;
	}

	public void publishBookUpdated(String isbn, int schemaVersion) {

        BookView book = queryRepository.findById(isbn).orElseThrow(() -> new BookNotFound(String.format("%s not registered", isbn)));

        publisher.publishBookUpdated(book, schemaVersion);
    }

}