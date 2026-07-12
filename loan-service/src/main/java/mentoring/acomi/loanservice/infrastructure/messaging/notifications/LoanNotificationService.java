package mentoring.acomi.loanservice.infrastructure.messaging.notifications;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.errors.LoanNotFound;
import mentoring.acomi.loanservice.application.errors.UserNotFound;
import mentoring.acomi.loanservice.application.repositories.LoanViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.application.view.UserView;

@Component
public class LoanNotificationService {

	private final LoanViewQueryRepository loanRepository;
	private final UserViewQueryRepository userRepository;
	private final LoanNotificationPublisher publisher;
	
	public LoanNotificationService(LoanViewQueryRepository loanRepository, UserViewQueryRepository userRepository, LoanNotificationPublisher publisher) {
		this.loanRepository = loanRepository;
		this.userRepository = userRepository;
		this.publisher = publisher;
	}
	
	public void publishLoanUpdated(String loanId, int schemaVersion) {
        LoanView loan = loanRepository.findById(loanId).orElseThrow(() -> new LoanNotFound("%s not found".formatted(loanId)));
        UserView user = userRepository.findById(loan.userId()).orElseThrow(() -> new UserNotFound("%s not found".formatted(loan.userId())));
        publisher.publishLoanUpdated(loan, user.identityProviderId(), schemaVersion);
    }
	
}
