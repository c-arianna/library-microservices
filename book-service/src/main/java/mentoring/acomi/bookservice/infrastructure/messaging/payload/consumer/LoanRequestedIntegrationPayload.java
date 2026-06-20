package mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer;

import java.time.LocalDate;

public record LoanRequestedIntegrationPayload(String loanId, String isbn, String userId, LocalDate start, LocalDate end) {}
