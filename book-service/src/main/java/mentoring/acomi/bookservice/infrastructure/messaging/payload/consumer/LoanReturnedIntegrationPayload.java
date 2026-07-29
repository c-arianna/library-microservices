package mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record LoanReturnedIntegrationPayload(@NotBlank String loanId, @NotBlank String isbn, @NotBlank String userId, LocalDate returnedAt) { }