package mentoring.acomi.loanservice.infrastructure.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.LoanIntegrationRepository;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandlerRegistry;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class LoanEventProcessor {

	private final LoanIntegrationRepository loanIntegrationRepository;
	private final LoanEventRepository loanEventRepository;
	private final EventHandlerRegistry registry;

	private final Logger logger = LogManager.getLogger(LoanEventProcessor.class);

	public LoanEventProcessor(LoanIntegrationRepository loanIntegrationRepository, LoanEventRepository loanEventRepository, EventHandlerRegistry registry) {
		this.loanIntegrationRepository = loanIntegrationRepository;
		this.loanEventRepository = loanEventRepository;
		this.registry = registry;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processConsumerEvent(IntegrationEventEnvelope<?> event) {

		if (loanIntegrationRepository.exists(event.eventId(), event.aggregateType())) {
			return;
		}

		try {
			loanIntegrationRepository.save(event);
		} catch (DataIntegrityViolationException e) {
			return;
		}

		handleEvent(event);
		loanEventRepository.markProcessed(event.eventId(), event.aggregateType());

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processProducerEvent(IntegrationEventEnvelope<?> event) {

		if (loanEventRepository.existsEventProcessed(event.eventId(), AggregateType.LOAN.name())) {
			return;
		}

		handleEvent(event);
		loanEventRepository.markProcessed(event.eventId(), event.aggregateType());

	}

	private void handleEvent(IntegrationEventEnvelope<?> eventEnvelope) {

		IntegrationEventTypes eventType = eventEnvelope.eventType();
		String eventId = eventEnvelope.eventId();

		if (eventType == IntegrationEventTypes.LOAN_CONFIRM_REQUESTED) {
			logger.info("No handle needed for process event {}", eventEnvelope.eventType());
			return;
		}

		logger.info("Processing event {}, ID: {}", eventType, eventId);

		EventHandler handler = registry.find(eventEnvelope)
				.orElseThrow(() -> new IllegalStateException("No handler found for %s version %d"
						.formatted(eventEnvelope.eventType(), eventEnvelope.schemaVersion())));

		handler.handleEvent(eventEnvelope);

		logger.info("Event {} processed successfully", eventEnvelope.eventId());
		
	}

}
