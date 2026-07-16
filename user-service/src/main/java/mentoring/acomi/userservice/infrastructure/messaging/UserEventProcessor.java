package mentoring.acomi.userservice.infrastructure.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandlerRegistry;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.domain.events.AggregateType;

@Component
public class UserEventProcessor {
		
	private final UserEventRepository userEventRepository;
	private final EventHandlerRegistry registry;
	
	private final Logger logger = LogManager.getLogger(UserEventProcessor.class);
	
	public UserEventProcessor(UserEventRepository userEventRepository, EventHandlerRegistry registry) {
		this.userEventRepository = userEventRepository;
		this.registry = registry;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processProducerEvent(IntegrationEventEnvelope<?> eventEnvelope) {
		
		if (userEventRepository.existsEventProcessed(eventEnvelope.eventId(), AggregateType.USER.name())) {
			return;
		}
		
		handleEvent(eventEnvelope);
		userEventRepository.markProcessed(eventEnvelope.eventId(), eventEnvelope.aggregateType());
		
	}
	
	private void handleEvent(IntegrationEventEnvelope<?> eventEnvelope) {

		IntegrationEventTypes eventType = eventEnvelope.eventType();
		String eventId = eventEnvelope.eventId();
				
		logger.info("Processing event {}, ID: {}", eventType, eventId);

    	EventHandler handler = registry.find(eventEnvelope).orElseThrow(() -> new IllegalStateException("No handler found for %s version %d"
    			                     .formatted(eventEnvelope.eventType(), eventEnvelope.schemaVersion())));
    	   	
    	handler.handleEvent(eventEnvelope);
		
    	logger.info("Event {} processed successfully", eventEnvelope.eventId());
	}
	
}
