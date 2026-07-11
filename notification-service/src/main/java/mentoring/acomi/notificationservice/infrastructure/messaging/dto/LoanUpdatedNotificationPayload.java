package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

import java.time.LocalDate;

public record LoanUpdatedNotificationPayload(String loanId, String isbn, String userId, String identityProviderId, LoanStatus status, LocalDate startDate, 
		LocalDate endDate) implements EventNotificationPayload{}