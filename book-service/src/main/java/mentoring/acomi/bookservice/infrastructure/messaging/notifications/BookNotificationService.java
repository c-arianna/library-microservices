package mentoring.acomi.bookservice.infrastructure.messaging.notifications;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.errors.BookNotFound;
import mentoring.acomi.bookservice.application.errors.BookRequestNotFound;
import mentoring.acomi.bookservice.application.repositories.BookRequestViewQueryRepository;
import mentoring.acomi.bookservice.application.repositories.BookSubscriptionRepository;
import mentoring.acomi.bookservice.application.repositories.BookViewQueryRepository;
import mentoring.acomi.bookservice.application.view.BookRequestView;
import mentoring.acomi.bookservice.application.view.BookView;

@Component
public class BookNotificationService {

    private final BookViewQueryRepository queryRepository;
    private final BookRequestViewQueryRepository requestQueryRepository;
    private final BookSubscriptionRepository subscriptionRepository;
    private final BookNotificationPublisher publisher;
    
    public BookNotificationService(BookViewQueryRepository queryRepository, BookRequestViewQueryRepository requestQueryRepository, 
    		BookNotificationPublisher publisher, BookSubscriptionRepository subscriptionRepository) {
		this.queryRepository = queryRepository;
		this.requestQueryRepository = requestQueryRepository;
		this.publisher = publisher;
		this.subscriptionRepository = subscriptionRepository;
	}

	public void publishBookUpdated(String isbn) {
        BookView book = queryRepository.findById(isbn).orElseThrow(() -> new BookNotFound(String.format("%s not registered", isbn)));
        publisher.publishBookUpdated(book);
    }
	
	public void notifyAvailability(String isbn) {

		 BookView book = queryRepository.findById(isbn).orElseThrow(() -> new BookNotFound(String.format("%s not registered", isbn)));
		 
         subscriptionRepository.findSubscriptionsToNotify(isbn)
               .forEach(subscription -> publisher.publishBookSubscriptionRequested(subscription, book.title()));
	}
	
	public void publishBookRequestUpdated(String requestId) {
        BookRequestView book = requestQueryRepository.findById(requestId).orElseThrow(() -> 
             new BookRequestNotFound(String.format("Book Request ID %s not found", requestId)));
        publisher.publishBookRequestUpdated(book);
    }

}