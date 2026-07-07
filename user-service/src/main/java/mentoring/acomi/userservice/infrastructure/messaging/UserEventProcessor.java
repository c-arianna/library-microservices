package mentoring.acomi.userservice.infrastructure.messaging;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.domain.events.AggregateType;

@Component
public class UserEventProcessor {
	
	private final Map<IntegrationEventTypes, List<EventHandler>> handlers;
	
	private final UserEventRepository userEventRepository;
			
	private final Logger logger = LogManager.getLogger(UserEventProcessor.class);
	
	public UserEventProcessor(UserEventRepository userEventRepository, List<EventHandler> handlers) {
		this.userEventRepository = userEventRepository;
		this.handlers = handlers.stream().collect(Collectors.groupingBy(EventHandler::eventType));
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

		List<EventHandler> eventHandlers = handlers.get(eventType);
		
		if (eventHandlers  == null) {
			 logger.warn("No handler found for event {} ({})", eventId, eventType);
			 return;
		}
		
		eventHandlers.stream().filter(h -> h.accepts(eventEnvelope)).findFirst()
			.ifPresentOrElse(h -> h.handleEvent(eventEnvelope), () -> logger.warn("No handler found for event {} with schemaVersion {}",
	                eventEnvelope.eventType(), eventEnvelope.schemaVersion()
	            )
	        );
		
		logger.info("Event {} processed successfully", eventEnvelope.eventId());
	}
	
}
