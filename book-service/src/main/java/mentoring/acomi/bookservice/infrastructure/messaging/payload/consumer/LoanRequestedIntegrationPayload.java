package mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoanRequestedIntegrationPayload(@NotBlank String loanId, @NotBlank String isbn, @NotBlank String userId, 
		@NotNull LocalDate start, @NotNull LocalDate end) {}
