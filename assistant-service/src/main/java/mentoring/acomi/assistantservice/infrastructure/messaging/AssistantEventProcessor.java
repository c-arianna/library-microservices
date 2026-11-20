package mentoring.acomi.assistantservice.infrastructure.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.assistantservice.infrastructure.persistence.repositories.AssistantNotificationRepository;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerRegistry;

@Component
public class AssistantEventProcessor {

	private final AssistantNotificationRepository assistantNotificationRepository;
	private final NotificationHandlerRegistry registry;
	
	private final Logger logger = LogManager.getLogger(AssistantEventProcessor.class);
	
	public AssistantEventProcessor(AssistantNotificationRepository assistantNotificationRepository, NotificationHandlerRegistry registry){
		this.assistantNotificationRepository = assistantNotificationRepository;
		this.registry = registry;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processConsumerEvent(NotificationEventEnvelope<?> event) {
		
		if (assistantNotificationRepository.exists(event.eventId())) {
			return;
		}

		try {
			assistantNotificationRepository.save(event);
		} catch (DataIntegrityViolationException e) {
			return;
		}
		
		handleEvent(event);
		assistantNotificationRepository.markProcessed(event.eventId(), aggregateType(event));	
		
	}
	
	private void handleEvent(NotificationEventEnvelope<?> eventEnvelope) {

    	NotificationEventType eventType = eventEnvelope.eventType();
		String eventId = eventEnvelope.eventId();
				
		logger.info("Processing event {}, ID: {}", eventType, eventId);
    	
    	NotificationHandler handler = registry.find(eventEnvelope)
    			                        .orElseThrow(() -> new IllegalStateException("No notification handler found for %s version %d"
    			                        .formatted(eventEnvelope.eventType(), eventEnvelope.schemaVersion())));
    	   	
    	handler.handleEvent(eventEnvelope);
		
    	logger.info("Event {} processed successfully", eventEnvelope.eventId());
    	    	
    }
	
	private String aggregateType(NotificationEventEnvelope<?> event) {

	    return switch (event.eventType()) {

	        case BOOK_UPDATED -> "BOOK";

	        case USER_UPDATED -> "USER";

	        case LOAN_UPDATED -> "LOAN";
		
	        default -> throw new IllegalArgumentException(String.format("Unexpected value: %s", event.eventType()));
	    };
	}

}
