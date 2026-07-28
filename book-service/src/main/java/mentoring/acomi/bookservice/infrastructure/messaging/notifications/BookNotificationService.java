package mentoring.acomi.bookservice.infrastructure.messaging.notifications;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.errors.BookNotFound;
import mentoring.acomi.bookservice.application.repositories.BookSubscriptionRepository;
import mentoring.acomi.bookservice.application.repositories.BookViewQueryRepository;
import mentoring.acomi.bookservice.application.view.BookView;

@Component
public class BookNotificationService {

    private final BookViewQueryRepository queryRepository;
    private final BookSubscriptionRepository subscriptionRepository;
    private final BookNotificationPublisher publisher;
    
    public BookNotificationService(BookViewQueryRepository queryRepository, BookNotificationPublisher publisher,
    		BookSubscriptionRepository subscriptionRepository) {
		this.queryRepository = queryRepository;
		this.publisher = publisher;
		this.subscriptionRepository = subscriptionRepository;
	}

	public void publishBookUpdated(String isbn, int schemaVersion) {
        BookView book = queryRepository.findById(isbn).orElseThrow(() -> new BookNotFound(String.format("%s not registered", isbn)));
        publisher.publishBookUpdated(book, schemaVersion);
    }
	
	 public void notifyAvailability(String isbn, int schemaVersion) {

		 BookView book = queryRepository.findById(isbn).orElseThrow(() -> new BookNotFound(String.format("%s not registered", isbn)));
		 
         subscriptionRepository.findSubscriptionsToNotify(isbn)
               .forEach(subscription -> publisher.publishBookSubscriptionRequested(subscription, book.title(), schemaVersion));
   }

}