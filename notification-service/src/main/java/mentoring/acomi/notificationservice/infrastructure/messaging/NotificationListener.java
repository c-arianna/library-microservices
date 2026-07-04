package mentoring.acomi.notificationservice.infrastructure.messaging;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;

@Component
public class NotificationListener {
	
	private final Logger logger = LogManager.getLogger(NotificationListener.class);
	
	private final Map<IntegrationEventTypes, List<EventHandler>> handlers;
	
	public NotificationListener(List<EventHandler> handlers) {
		this.handlers = handlers.stream().collect(Collectors.groupingBy(EventHandler::eventType));
	}
	
	@RabbitListener(queues = MessagingTopology.NOTIFICATION_QUEUE)
	public void onEvent(IntegrationEventEnvelope<?> eventEnvelope) {
		IntegrationEventTypes eventType = eventEnvelope.eventType();
		
		logger.info("Processing event {} ({})", eventEnvelope.eventId(), eventType);
		
		List<EventHandler> eventHandlers = handlers.get(eventEnvelope.eventType());
		
		if (eventHandlers  == null) {
			 logger.warn("No handler found for event {} ({})", eventEnvelope.eventId(), eventType);
			 return;
		}
		
		try {
			getHandler(eventEnvelope)
			.ifPresentOrElse(h -> h.handleEvent(eventEnvelope), () -> logger.warn("No handler found for event {} with schemaVersion {}",
	                eventEnvelope.eventType(), eventEnvelope.schemaVersion()
	            )
	        );
		} catch (NotificationHandlingException ex) {
		    logger.warn("Notification discarded: Notification discarded for event {}: {}", eventEnvelope.eventId(), ex.getMessage());
		} catch (Exception ex) {
		    logger.error("Unexpected notification error", ex);
		    throw ex;
		}
	}
	
	private Optional<EventHandler> getHandler(IntegrationEventEnvelope<?> event) {

	    List<EventHandler> eventHandlers = handlers.get(event.eventType());

	    if (eventHandlers == null) {
	        return Optional.empty();
	    }
	    
	    return eventHandlers.stream().filter(h -> h.accepts(event)).findFirst();
	}

}
