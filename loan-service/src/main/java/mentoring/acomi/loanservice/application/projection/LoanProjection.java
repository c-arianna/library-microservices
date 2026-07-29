package mentoring.acomi.loanservice.application.projection;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.stereotype.Component;
import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;

@Component
public class LoanProjection implements LoanProjectionOperations {

	private final LoanViewRepository repository;

	public LoanProjection(LoanViewRepository repository) {
		this.repository = repository;
	}

	@Override
	public void loanInsert(LoanRequestedIntegrationPayload payload, Instant occurredAt) {
		repository.insertRequest(getLoan(payload), occurredAt);
	}
	
	@Override
	public void confirmLoan(String loanId, Instant occurredAt) {
		repository.updateStatus(loanId, LoanStatus.CONFIRMED, occurredAt);
	}
	
	@Override
	public void cancelLoan(String loanId, Instant occurredAt) {
		repository.updateStatus(loanId, LoanStatus.CANCELED, occurredAt);
	}
	
	@Override
	public void returnLoan(String loanId, Instant occurredAt, LocalDate returnedAt) {
		repository.returnLoan(loanId, occurredAt, returnedAt);
	}
	
	@Override
	public void reserveLoan(String loanId, Instant occurredAt) {
		repository.updateStatus(loanId, LoanStatus.RESERVED, occurredAt);
	}
	
	@Override
	public void failLoan(String loanId, Instant occurredAt) {
		repository.updateStatus(loanId, LoanStatus.FAILED, occurredAt);
	}
	
	private LoanView getLoan(LoanRequestedIntegrationPayload payload) {
		return new LoanView(payload.loanId(), payload.isbn(), payload.userId(), payload.start(), payload.end(), LoanStatus.PENDING, null);
	}
}