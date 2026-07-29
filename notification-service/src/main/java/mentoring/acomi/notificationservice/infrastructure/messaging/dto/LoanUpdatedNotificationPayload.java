package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

import java.time.LocalDate;

public record LoanUpdatedNotificationPayload(String loanId, String isbn, String userId, String identityProviderId, 
		String cardNumber, LoanStatus status, LocalDate startDate, LocalDate endDate, boolean overdue, long daysOverdue) 
     implements EventNotificationPayload{}