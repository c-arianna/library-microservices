package mentoring.acomi.loanservice.infrastructure.messaging;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class LoanEventProcessor {

	private final Map<IntegrationEventTypes, List<EventHandler>> handlers;
	private final LoanIntegrationRepository loanIntegrationRepository;
	private final LoanEventRepository loanEventRepository;
	
	private final Logger logger = LogManager.getLogger(LoanEventProcessor.class);

	public LoanEventProcessor(LoanIntegrationRepository loanIntegrationRepository, LoanEventRepository loanEventRepository,
			List<EventHandler> handlers) {
		this.loanIntegrationRepository = loanIntegrationRepository;
		this.loanEventRepository = loanEventRepository;
		this.handlers = handlers.stream().collect(Collectors.groupingBy(EventHandler::eventType));
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
		
		if(eventType == IntegrationEventTypes.LOAN_CONFIRM_REQUESTED) {
			logger.info("No handle needed for process event {}", eventEnvelope.eventType());
			return;
		}
		
		logger.info("Processing event {}, ID: {}", eventType, eventId);

		List<EventHandler> eventHandlers = handlers.get(eventType);
		
		if (eventHandlers  == null) {
			 throw new IllegalStateException("No handler defined for event %s".formatted(eventType));
		}
		
		eventHandlers.stream().filter(h -> h.accepts(eventEnvelope)).findFirst()
			.ifPresentOrElse(h -> h.handleEvent(eventEnvelope), 
					() -> {  
						throw new IllegalStateException("No handler found for event %s with schemaVersion %s".formatted(eventEnvelope.eventType(),
								eventEnvelope.schemaVersion()));
					}
	            );
		
		
		logger.info("Event {} processed successfully", eventEnvelope.eventId());
	}
	    
}
