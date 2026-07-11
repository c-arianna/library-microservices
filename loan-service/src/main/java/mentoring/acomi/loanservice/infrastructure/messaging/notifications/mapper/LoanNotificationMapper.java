package mentoring.acomi.loanservice.infrastructure.messaging.notifications.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.infrastructure.messaging.notifications.payload.LoanUpdatedNotificationPayload;

@Component
public class LoanNotificationMapper {

	public LoanUpdatedNotificationPayload map(LoanView loan, String identityProviderId) {
		return new LoanUpdatedNotificationPayload(loan.id(), loan.isbn(), loan.userId(), identityProviderId, loan.status(), loan.start(), loan.end());
    }
}
