package mentoring.acomi.assistantservice.infrastructure.messaging.notifications.payload;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mentoring.acomi.sharedcorelibrary.model.LoanStatus;

public record LoanUpdatedNotificationPayload(@NotBlank String loanId, @NotBlank String isbn, @NotBlank String author, @NotBlank String title, 
		@NotBlank String userId, @NotBlank String identityProviderId, String cardNumber, @NotNull LoanStatus status, 
		@NotNull LocalDate startDate, @NotNull LocalDate endDate, boolean overdue, long daysOverdue) {}