package mentoring.acomi.loanservice.application.projection;

import java.time.Instant;
import java.time.LocalDate;

import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;

public interface LoanProjectionOperations {
	void loanInsert(LoanRequestedIntegrationPayload payload, Instant occurredAt);
	void confirmLoan(String loanId, Instant occurredAt);
	void cancelLoan(String loanId, Instant occurredAt);
	void returnLoan(String loanId, Instant occurredAt, LocalDate returnedAt);
	void reserveLoan(String loanId, Instant occurredAt);
	void failLoan(String loanId, Instant occurredAt);

}
