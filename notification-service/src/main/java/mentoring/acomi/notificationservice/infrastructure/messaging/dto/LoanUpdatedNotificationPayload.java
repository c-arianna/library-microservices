package mentoring.acomi.notificationservice.infrastructure.messaging.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoanUpdatedNotificationPayload(@NotBlank String loanId, @NotBlank String isbn, @NotBlank String userId, 
		@NotBlank String identityProviderId, String cardNumber, @NotNull LoanStatus status,  @NotNull LocalDate startDate, 
		 @NotNull LocalDate endDate, boolean overdue, long daysOverdue) 
     implements EventNotificationPayload{}