package mentoring.acomi.bookservice.infrastructure.messaging.notifications.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.payload.BookUpdatedNotificationPayload;

@Component
public class BookNotificationMapper {

    public BookUpdatedNotificationPayload map(BookView book) {
    	boolean bookAvailable = book.totalCopies() - book.borrowedCopies() - book.reservedCopies() > 0;
        return new BookUpdatedNotificationPayload(book.isbn(), book.author(), book.title(), book.description(), bookAvailable, 
        		book.totalCopies(), book.borrowedCopies(), book.reservedCopies());
    }

}
