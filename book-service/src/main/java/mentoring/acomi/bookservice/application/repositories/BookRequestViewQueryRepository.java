package mentoring.acomi.bookservice.application.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.bookservice.application.view.BookRequestView;

public interface BookRequestViewQueryRepository {
	Optional<BookRequestView> findPendingRequestByIsbn(String isbn);
	Optional<BookRequestView> findPendingRequestByAuthorAndTitle(String author, String title);
	List<BookRequestView> findBookRequests();
	Optional<BookRequestView> findById(String requestId);
}
