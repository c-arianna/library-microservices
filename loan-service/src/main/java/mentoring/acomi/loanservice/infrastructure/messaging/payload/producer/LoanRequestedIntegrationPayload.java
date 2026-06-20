package mentoring.acomi.loanservice.infrastructure.messaging.payload.producer;

import java.time.LocalDate;

public record LoanRequestedIntegrationPayload(String loanId, String isbn, String userId, LocalDate start, LocalDate end) {}
