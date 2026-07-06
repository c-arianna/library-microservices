package mentoring.acomi.loanservice.application.projection;

import java.time.Instant;

import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;

public class AbstractLoanProjection {
	
	private final LoanViewRepository repository;

	public AbstractLoanProjection(LoanViewRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void loanInsert(LoanRequestedIntegrationPayload payload, Instant occurredAt) {
		repository.insertRequest(getLoan(payload), occurredAt);
	}
	
	@Transactional
	public void confirmLoan(String loanId, Instant occurredAt) {
		repository.updateStatus(loanId, LoanStatus.CONFIRMED, occurredAt);
	}
	
	@Transactional
	public void cancelLoan(String loanId, Instant occurredAt) {
		repository.updateStatus(loanId, LoanStatus.CANCELED, occurredAt);
	}
	
	@Transactional
	public void returnLoan(String loanId, Instant occurredAt) {
		repository.updateStatus(loanId, LoanStatus.RETURNED, occurredAt);
	}
	
	@Transactional
	public void reserveLoan(String loanId, Instant occurredAt) {
		repository.updateStatus(loanId, LoanStatus.RESERVED, occurredAt);
	}
	
	@Transactional
	public void failLoan(String loanId, Instant occurredAt) {
		repository.updateStatus(loanId, LoanStatus.FAILED, occurredAt);
	}
	
	private LoanView getLoan(LoanRequestedIntegrationPayload payload) {
		return new LoanView(payload.loanId(), payload.isbn(), payload.userId(), payload.start(), payload.end(), LoanStatus.PENDING);
	}
}
