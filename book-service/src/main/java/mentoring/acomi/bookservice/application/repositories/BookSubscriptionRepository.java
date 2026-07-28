package mentoring.acomi.bookservice.application.repositories;

import java.time.Instant;
import java.util.List;

import mentoring.acomi.bookservice.application.view.BookSubscriptionView;

public interface BookSubscriptionRepository {
     void add(BookSubscriptionView subscription);
     void markAsNotified(long subscriptionId, Instant notifyAt);
     List<BookSubscriptionView> findSubscriptionsToNotify(String isbn);
     List<BookSubscriptionView> getSubscriptions(String isbn);
}
