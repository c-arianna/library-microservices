package mentoring.acomi.loanservice.infrastructure.messaging.notifications.payload;

import java.time.LocalDate;

import mentoring.acomi.loanservice.domain.model.LoanStatus;

public record LoanUpdatedNotificationPayload(String loanId, String isbn, String userId, String identityProviderId, String cardNumber,
		LoanStatus status, LocalDate startDate, LocalDate endDate, boolean overdue, long daysOverdue) {}