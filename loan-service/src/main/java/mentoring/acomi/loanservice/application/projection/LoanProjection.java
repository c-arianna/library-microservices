package mentoring.acomi.loanservice.application.projection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.domain.events.LoanCanceledEvent;
import mentoring.acomi.loanservice.domain.events.LoanConfirmedEvent;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanFailedEvent;
import mentoring.acomi.loanservice.domain.events.LoanRequestedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReservedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReturnedEvent;

import mentoring.acomi.loanservice.domain.events.payload.LoanRequestPayload;
import mentoring.acomi.loanservice.domain.model.DateRange;
import mentoring.acomi.loanservice.domain.model.LoanStatus;

@Component
public class LoanProjection {

	private final LoanViewRepository repository;

	public LoanProjection(LoanViewRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void updateView(LoanEvent event) {

		switch (event) {
			case LoanRequestedEvent e -> repository.insertRequest(getLoan(e.payload()));
			case LoanFailedEvent e -> repository.updateStatus(e.payload().id(), LoanStatus.FAILED);
			case LoanReservedEvent e -> repository.updateStatus(e.payload().id(), LoanStatus.RESERVED);
			case LoanConfirmedEvent e -> repository.updateStatus(e.payload().id(), LoanStatus.CONFIRMED);
			case LoanCanceledEvent e -> repository.updateStatus(e.payload().id(), LoanStatus.CANCELED);
			case LoanReturnedEvent e -> repository.updateStatus(e.payload().id(), LoanStatus.RETURNED);
		default -> throw new IllegalArgumentException("Unexpected value: " + event);
			
		}

	}

	private LoanView getLoan(LoanRequestPayload payload) {
		DateRange period = payload.period();
		return new LoanView(payload.id(), payload.isbn(), payload.userId(), period.getStart(), period.getEnd(), payload.status());
	}

}