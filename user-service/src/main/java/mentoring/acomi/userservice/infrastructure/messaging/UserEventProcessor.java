package mentoring.acomi.userservice.infrastructure.messaging;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandlerRegistry;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.domain.events.AggregateType;
import mentoring.acomi.userservice.infrastructure.messaging.notifications.UserNotificationService;

@Component
public class UserEventProcessor {
		
	private final UserEventRepository userEventRepository;
	private final EventHandlerRegistry registry;
	private final UserNotificationService notificationService;
	
	private final Logger logger = LogManager.getLogger(UserEventProcessor.class);
	
	public UserEventProcessor(UserEventRepository userEventRepository, EventHandlerRegistry registry, UserNotificationService notificationService) {
		this.userEventRepository = userEventRepository;
		this.registry = registry;
		this.notificationService = notificationService;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processProducerEvent(IntegrationEventEnvelope<?> eventEnvelope) {
		
		if (userEventRepository.existsEventProcessed(eventEnvelope.eventId(), AggregateType.USER.name())) {
			return;
		}
		
		Optional<ProjectionUpdateNotification> notification = handleEvent(eventEnvelope);
		
		if(notification.isPresent()){
			notificationService.publishUserUpdated(notification.get().aggregateId(), notification.get().schemaVersion());
		}
		
		userEventRepository.markProcessed(eventEnvelope.eventId(), eventEnvelope.aggregateType());
		
	}
	
	private Optional<ProjectionUpdateNotification> handleEvent(IntegrationEventEnvelope<?> eventEnvelope) {

		IntegrationEventTypes eventType = eventEnvelope.eventType();
		String eventId = eventEnvelope.eventId();
				
		logger.info("Processing event {}, ID: {}", eventType, eventId);

    	EventHandler handler = registry.find(eventEnvelope).orElseThrow(() -> new IllegalStateException("No handler found for %s version %d"
    			                     .formatted(eventEnvelope.eventType(), eventEnvelope.schemaVersion())));
    	   	
    	Optional<ProjectionUpdateNotification> notification = handler.handleEvent(eventEnvelope);
		
    	logger.info("Event {} processed successfully", eventEnvelope.eventId());
    	
    	return notification;
	}
	
}
