package mentoring.acomi.loanservice.application.projection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.view.LoanView;

import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;

@Component
public class LoanProjection {

	private final LoanViewRepository repository;

	public LoanProjection(LoanViewRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void loanInsert(LoanRequestedIntegrationPayload payload) {
		repository.insertRequest(getLoan(payload));
	}
	
	@Transactional
	public void confirmLoan(LoanIntegrationPayload payload) {
		repository.updateStatus(payload.loanId(), LoanStatus.CONFIRMED);
	}
	
	@Transactional
	public void cancelLoan(LoanIntegrationPayload payload) {
		repository.updateStatus(payload.loanId(), LoanStatus.CANCELED);
	}
	
	@Transactional
	public void returnLoan(LoanIntegrationPayload payload) {
		repository.updateStatus(payload.loanId(), LoanStatus.RETURNED);
	}
	
	@Transactional
	public void reserveLoan(LoanIntegrationPayload payload) {
		repository.updateStatus(payload.loanId(), LoanStatus.RESERVED);
	}
	
	@Transactional
	public void failLoan(LoanIntegrationPayload payload) {
		repository.updateStatus(payload.loanId(), LoanStatus.FAILED);
	}
	
	private LoanView getLoan(LoanRequestedIntegrationPayload payload) {
		return new LoanView(payload.loanId(), payload.isbn(), payload.userId(), payload.start(), payload.end(), LoanStatus.PENDING);
	}

}