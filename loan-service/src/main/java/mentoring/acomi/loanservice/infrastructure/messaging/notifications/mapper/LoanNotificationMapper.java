package mentoring.acomi.loanservice.infrastructure.messaging.notifications.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.messaging.notifications.payload.LoanUpdatedNotificationPayload;

@Component
public class LoanNotificationMapper {

	public LoanUpdatedNotificationPayload map(LoanView loan, String identityProviderId, String cardNumber) {
		LocalDate now = LocalDate.now();
		boolean overdue = loan.status() == LoanStatus.CONFIRMED && now.isAfter(loan.end());
		long overdueDays = overdue ? Math.max(0, ChronoUnit.DAYS.between(loan.end(), now)) : 0;
		return new LoanUpdatedNotificationPayload(loan.id(), loan.isbn(), loan.userId(), identityProviderId, cardNumber, loan.status(), 
				loan.start(), loan.end(), overdue, overdueDays);
    }
}
