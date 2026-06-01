package mentoring.acomi.loanservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.domain.events.LoanCanceledEvent;
import mentoring.acomi.loanservice.domain.events.LoanConfirmRequestedEvent;
import mentoring.acomi.loanservice.domain.events.LoanConfirmedEvent;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanFailedEvent;
import mentoring.acomi.loanservice.domain.events.LoanRequestedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReservedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReturnedEvent;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.integration.messaging.loan.LoanFailedIntegrationPayload;
import mentoring.acomi.sharedlibrary.integration.messaging.loan.LoanIntegrationPayload;

@Component
public class LoanIntegrationEventMapper {
	
	private static final String PRODUCER = "loan-service";
	
	public IntegrationEventEnvelope<?> map(LoanEvent event) {
		
		return switch(event) {
		case LoanRequestedEvent e -> {
			yield getLoanRequestedIntegrationEvent(e);
		}
		
		case LoanReservedEvent e -> {
			yield getLoanReservedIntegrationEvent(e);
		}
		
		case LoanConfirmedEvent e -> {
			yield getLoanConfirmedIntegrationEvent(e);
		}
		
		case LoanCanceledEvent e -> {
			yield getLoanCanceledIntegrationEvent(e);
		}
		
		case LoanReturnedEvent e -> {
			yield getLoanReturnedIntegrationEvent(e);
		}
		
		case LoanFailedEvent e -> {
			yield getLoanFailedIntegrationEvent(e);
		}
		
		case LoanConfirmRequestedEvent e -> {
			yield getLoanConfirmRequestedIntegrationEvent(e);
		}
		};
	}

	private IntegrationEventEnvelope<?> getLoanConfirmRequestedIntegrationEvent(LoanConfirmRequestedEvent e) {
		LoanIntegrationPayload payload = new LoanIntegrationPayload(e.payload().id(), e.payload().isbn(), e.payload().userId());
		return new IntegrationEventEnvelope<>(e.eventId(), IntegrationEventTypes.LOAN_CONFIRM_REQUESTED, PRODUCER, e.aggregateId(), e.occurredAt(), payload);
	}

	private IntegrationEventEnvelope<?> getLoanFailedIntegrationEvent(LoanFailedEvent e) {
		LoanFailedIntegrationPayload payload = new LoanFailedIntegrationPayload(e.payload().id(), e.payload().reason().toString());
		return new IntegrationEventEnvelope<>(e.eventId(), IntegrationEventTypes.LOAN_FAILED, PRODUCER, e.aggregateId(), e.occurredAt(), payload);
	}

	private IntegrationEventEnvelope<?> getLoanReturnedIntegrationEvent(LoanReturnedEvent e) {
		LoanIntegrationPayload payload = new LoanIntegrationPayload(e.payload().id(), e.payload().isbn(), e.payload().userId());
		return new IntegrationEventEnvelope<>(e.eventId(), IntegrationEventTypes.LOAN_RETURNED, PRODUCER, e.aggregateId(), e.occurredAt(), payload);
	}

	private IntegrationEventEnvelope<?> getLoanCanceledIntegrationEvent(LoanCanceledEvent e) {
		LoanIntegrationPayload payload = new LoanIntegrationPayload(e.payload().id(), e.payload().isbn(), e.payload().userId());
		return new IntegrationEventEnvelope<>(e.eventId(), IntegrationEventTypes.LOAN_CANCELED, PRODUCER, e.aggregateId(), e.occurredAt(), payload);
	}

	private IntegrationEventEnvelope<?> getLoanConfirmedIntegrationEvent(LoanConfirmedEvent e) {
		LoanIntegrationPayload payload = new LoanIntegrationPayload(e.payload().id(), e.payload().isbn(), e.payload().userId());
		return new IntegrationEventEnvelope<>(e.eventId(), IntegrationEventTypes.LOAN_CONFIRMED, PRODUCER, e.aggregateId(), e.occurredAt(), payload);
	}

	private IntegrationEventEnvelope<?> getLoanReservedIntegrationEvent(LoanReservedEvent e) {
		LoanIntegrationPayload payload = new LoanIntegrationPayload(e.payload().id(), e.payload().isbn(), e.payload().userId());
		return new IntegrationEventEnvelope<>(e.eventId(), IntegrationEventTypes.LOAN_RESERVED, PRODUCER, e.aggregateId(), e.occurredAt(), payload);
	}

	private IntegrationEventEnvelope<?> getLoanRequestedIntegrationEvent(LoanRequestedEvent e) {
		LoanIntegrationPayload payload = new LoanIntegrationPayload(e.payload().id(), e.payload().isbn(), e.payload().userId());
		return new IntegrationEventEnvelope<>(e.eventId(), IntegrationEventTypes.LOAN_REQUESTED, PRODUCER, e.aggregateId(), e.occurredAt(), payload);
	}
		

}
