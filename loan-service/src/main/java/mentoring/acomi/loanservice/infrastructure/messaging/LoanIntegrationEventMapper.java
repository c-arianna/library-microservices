package mentoring.acomi.loanservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.domain.events.LoanCanceledEvent;
import mentoring.acomi.loanservice.domain.events.LoanConfirmRequestedEvent;
import mentoring.acomi.loanservice.domain.events.LoanConfirmedEvent;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanFailedEvent;
import mentoring.acomi.loanservice.domain.events.LoanRequestedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReservedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReturnedEvent;
import mentoring.acomi.loanservice.domain.events.payload.LoanRequestPayload;
import mentoring.acomi.loanservice.domain.events.payload.LoanReturnedPayload;
import mentoring.acomi.loanservice.domain.model.DateRange;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanFailedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanReturnedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;


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
		return envelope(e, IntegrationEventTypes.LOAN_CONFIRM_REQUESTED, LoanIntegrationPublisherEventVersions.LOAN_CONFIRM_REQUESTED, payload);
	}

	private IntegrationEventEnvelope<?> getLoanFailedIntegrationEvent(LoanFailedEvent e) {
		LoanFailedIntegrationPayload payload = new LoanFailedIntegrationPayload(e.payload().id(), e.payload().reason().toString());
		return envelope(e, IntegrationEventTypes.LOAN_FAILED, LoanIntegrationPublisherEventVersions.LOAN_FAILED, payload);
	}

	private IntegrationEventEnvelope<?> getLoanReturnedIntegrationEvent(LoanReturnedEvent e) {
		LoanReturnedPayload domainPayload = e.payload();
		LoanReturnedIntegrationPayload payload = new LoanReturnedIntegrationPayload(domainPayload.id(), domainPayload.isbn(), 
				domainPayload.userId(), domainPayload.returnedAt());
		return envelope(e, IntegrationEventTypes.LOAN_RETURNED, LoanIntegrationPublisherEventVersions.LOAN_RETURNED, payload);
	}

	private IntegrationEventEnvelope<?> getLoanCanceledIntegrationEvent(LoanCanceledEvent e) {
		LoanIntegrationPayload payload = new LoanIntegrationPayload(e.payload().id(), e.payload().isbn(), e.payload().userId());
		return envelope(e, IntegrationEventTypes.LOAN_CANCELED, LoanIntegrationPublisherEventVersions.LOAN_CANCELED, payload);
	}

	private IntegrationEventEnvelope<?> getLoanConfirmedIntegrationEvent(LoanConfirmedEvent e) {
		LoanIntegrationPayload payload = new LoanIntegrationPayload(e.payload().id(), e.payload().isbn(), e.payload().userId());
		return envelope(e, IntegrationEventTypes.LOAN_CONFIRMED, LoanIntegrationPublisherEventVersions.LOAN_CONFIRMED, payload);
	}

	private IntegrationEventEnvelope<?> getLoanReservedIntegrationEvent(LoanReservedEvent e) {
		LoanIntegrationPayload payload = new LoanIntegrationPayload(e.payload().id(), e.payload().isbn(), e.payload().userId());
		return envelope(e, IntegrationEventTypes.LOAN_RESERVED, LoanIntegrationPublisherEventVersions.LOAN_RESERVED, payload);
	}

	private IntegrationEventEnvelope<?> getLoanRequestedIntegrationEvent(LoanRequestedEvent e) {
		LoanRequestPayload eventPayload = e.payload();
		DateRange period = eventPayload.period();
		LoanRequestedIntegrationPayload payload = new LoanRequestedIntegrationPayload(eventPayload.id(), eventPayload.isbn(), eventPayload.userId(),
				period.getStart(), period.getEnd());
		return envelope(e, IntegrationEventTypes.LOAN_REQUESTED, LoanIntegrationPublisherEventVersions.LOAN_REQUESTED, payload);
	}
	
	private <T> IntegrationEventEnvelope<T> envelope(LoanEvent e, IntegrationEventTypes type, int version, T payload) {
		return new IntegrationEventEnvelope<>(e.eventId(), type, PRODUCER, e.aggregateId(), AggregateType.LOAN.name(), e.eventVersion(), e.occurredAt(), version, payload);
	}
		

}
