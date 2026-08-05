package mentoring.acomi.loanservice.infrastructure.messaging.notifications;

import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.errors.LoanNotFound;
import mentoring.acomi.loanservice.application.errors.UserNotFound;
import mentoring.acomi.loanservice.application.repositories.BookViewRepository;
import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.loanservice.application.view.BookView;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.application.view.UserView;

@Component
public class LoanNotificationService {

	private final LoanViewQueryRepository loanRepository;
	private final UserViewQueryRepository userRepository;
	private final BookViewRepository bookRepository;
	private final LoanNotificationPublisher publisher;
	
	public LoanNotificationService(LoanViewQueryRepository loanRepository, UserViewQueryRepository userRepository, 
			BookViewRepository bookRepository, LoanNotificationPublisher publisher) {
		this.loanRepository = loanRepository;
		this.userRepository = userRepository;
		this.bookRepository = bookRepository;
		this.publisher = publisher;
	}
	
	public void publishLoanUpdated(String loanId) {
        LoanView loan = loanRepository.findById(loanId).orElseThrow(() -> new LoanNotFound("%s not found".formatted(loanId)));
        UserView user = userRepository.findById(loan.userId()).orElseThrow(() -> new UserNotFound("%s not found".formatted(loan.userId())));
        Optional<BookView> book = bookRepository.findByIsbn(loan.isbn());
        BookView bookView = book.isPresent() ? book.get() : new BookView(loan.isbn(), "", "");
        publisher.publishLoanUpdated(loan, bookView, user.identityProviderId(), user.cardNumber());
    }
	
}
