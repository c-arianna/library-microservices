package mentoring.acomi.bookservice.application.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.bookservice.application.view.BookRequestView;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;

public interface BookRequestViewQueryRepository {
	Optional<BookRequestView> findPendingRequestByIsbn(String isbn);
	Optional<BookRequestView> findPendingRequestByAuthorAndTitle(String author, String title);
	List<BookRequestView> findBookRequests();
	Optional<BookRequestView> findById(String requestId);
	List<BookRequestView> findPurchasableRequests();
	long countByStatus(BookRequestStatus status);
}
