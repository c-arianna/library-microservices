package mentoring.acomi.loanservice.application.aggregates;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanEventType;
import mentoring.acomi.loanservice.infrastructure.messaging.LoanIntegrationPublisherEventVersions;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxEvent;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxRepository;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxStatus;

@Component
public class LoanAggregateFactory {
	
	private final LoanEventRepository loanEventRepository;
	private final OutboxRepository outboxRepository;
	
	public LoanAggregateFactory(LoanEventRepository loanEventRepository, OutboxRepository outboxRepository) {
		this.loanEventRepository = loanEventRepository;
		this.outboxRepository = outboxRepository;
	}
	
	public LoanAggregate create(String userId) {
		List<LoanEvent> events = loanEventRepository.loadStream(userId);
		Consumer<LoanEvent> dispatcher = this::persistEvent;
		return new LoanAggregate(userId, dispatcher, events);
	}
	
	private void persistEvent(LoanEvent event) {
		loanEventRepository.appendToStream(event, getSchemaVersion(event.type()));
		OutboxEvent pendingOutbox = new OutboxEvent(event.eventId(), event.aggregateType(), OutboxStatus.PENDING, 0, null, 
				Instant.now(), null, Instant.now());
		outboxRepository.add(pendingOutbox);
	}
	
	private int getSchemaVersion(LoanEventType type) {

		return switch(type) {
		
			case LoanRequested -> {
				yield LoanIntegrationPublisherEventVersions.LOAN_REQUESTED;
			}
			case LoanFailed -> {
				yield LoanIntegrationPublisherEventVersions.LOAN_FAILED;
			}
			case LoanReserved -> {
				yield LoanIntegrationPublisherEventVersions.LOAN_RESERVED;
			}
			case LoanConfirmed -> {
				yield LoanIntegrationPublisherEventVersions.LOAN_CONFIRMED;
			}
			case LoanCanceled -> {
				yield LoanIntegrationPublisherEventVersions.LOAN_CANCELED;
			}
			case LoanReturned -> {
				yield LoanIntegrationPublisherEventVersions.LOAN_RETURNED;
			}
			case LoanConfirmRequested -> {
				yield LoanIntegrationPublisherEventVersions.LOAN_CONFIRM_REQUESTED;
			}
		};
	}

}
