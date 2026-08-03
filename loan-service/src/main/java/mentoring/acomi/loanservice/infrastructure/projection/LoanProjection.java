package mentoring.acomi.loanservice.infrastructure.projection;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.EventProjector;
import mentoring.acomi.loanservice.application.repositories.LoanViewRepository;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanFailedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanReturnedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class LoanProjection implements EventProjector {

	private final Map<IntegrationEventTypes, Consumer<IntegrationEventEnvelope<?>>> handlers;
    private final LoanViewRepository repository;
       
	public LoanProjection(LoanViewRepository repository) {
		this.repository = repository;
		handlers = Map.of(IntegrationEventTypes.LOAN_REQUESTED, this::loanInsert,
				IntegrationEventTypes.LOAN_CONFIRMED, this::confirmLoan,
				IntegrationEventTypes.LOAN_RESERVED, this::reserveLoan,
				IntegrationEventTypes.LOAN_RETURNED, this::returnLoan,
				IntegrationEventTypes.LOAN_CANCELED, this::cancelLoan,
				IntegrationEventTypes.LOAN_FAILED, this::failLoan);
	}

	@Override
	public boolean supports(IntegrationEventTypes type) {
		return handlers.containsKey(type);
	}

	@Override
	public void project(IntegrationEventEnvelope<?> event) {
		handlers.get(event.eventType()).accept(event);
	}

	private void loanInsert(IntegrationEventEnvelope<?> event) {
		LoanRequestedIntegrationPayload payload = (LoanRequestedIntegrationPayload) event.payload();
		repository.insertRequest(getLoan(payload), event.occurredAt());
	}

	private void confirmLoan(IntegrationEventEnvelope<?> event) {
		LoanIntegrationPayload payload = (LoanIntegrationPayload) event.payload();
		repository.updateStatus(payload.loanId(), LoanStatus.CONFIRMED, event.occurredAt());
	}

	private void cancelLoan(IntegrationEventEnvelope<?> event) {
		LoanIntegrationPayload payload = (LoanIntegrationPayload) event.payload();
		repository.updateStatus(payload.loanId(), LoanStatus.CANCELED, event.occurredAt());
	}

	private void returnLoan(IntegrationEventEnvelope<?> event) {
		LoanReturnedIntegrationPayload payload =  (LoanReturnedIntegrationPayload) event.payload();
		LocalDate returnedAt = event.schemaVersion() == 1 ? event.occurredAt().atZone(ZoneOffset.UTC).toLocalDate() : payload.returnedAt();
		repository.returnLoan(payload.loanId(), event.occurredAt(), returnedAt);
	}

	private void reserveLoan(IntegrationEventEnvelope<?> event) {
		LoanIntegrationPayload payload = (LoanIntegrationPayload) event.payload();
		repository.updateStatus(payload.loanId(), LoanStatus.RESERVED, event.occurredAt());
	}

	private void failLoan(IntegrationEventEnvelope<?> event) {
		LoanFailedIntegrationPayload payload = (LoanFailedIntegrationPayload) event.payload();
		repository.updateStatus(payload.loanId(), LoanStatus.FAILED, event.occurredAt());
	}

	private LoanView getLoan(LoanRequestedIntegrationPayload payload) {
		return new LoanView(payload.loanId(), payload.isbn(), payload.userId(), payload.start(), payload.end(),
				LoanStatus.PENDING, null);
	}
}