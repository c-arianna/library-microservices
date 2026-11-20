package mentoring.acomi.loanservice.infrastructure.messaging.notifications.payload;

import java.time.LocalDate;

import mentoring.acomi.sharedcorelibrary.model.LoanStatus;

public record LoanUpdatedNotificationPayload(String loanId, String isbn, String author, String title, String userId, String identityProviderId, String cardNumber,
		LoanStatus status, LocalDate startDate, LocalDate endDate, boolean overdue, long daysOverdue) {}