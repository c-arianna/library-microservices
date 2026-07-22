package mentoring.acomi.notificationservice.infrastructure.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
import mentoring.acomi.notificationservice.infrastructure.messaging.handlers.NotificationHandlerRegistry;
import mentoring.acomi.notificationservice.infrastructure.messaging.handlers.NotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

@Component
public class NotificationListener {
	
	private final Logger logger = LogManager.getLogger(NotificationListener.class);
		
	private final NotificationHandlerRegistry registry;
	
	public NotificationListener(NotificationHandlerRegistry registry) {
		this.registry = registry;
	}
	
	@RabbitListener(queues = MessagingTopology.NOTIFICATION_QUEUE)
	public void onEvent(NotificationEventEnvelope<?> notificationEvent) {
		
		NotificationEventType eventType = notificationEvent.eventType();
		
		logger.info("Processing event {} ({})", notificationEvent.eventId(), eventType);
		
		try {
			
			NotificationHandler handler = registry.find(notificationEvent)
					.orElseThrow(() -> new IllegalStateException("No handler found for %s version %d"
	                .formatted(notificationEvent.eventType(), notificationEvent.schemaVersion())));
	
			handler.handleEvent(notificationEvent);
	
			logger.info("Event {} processed successfully", notificationEvent.eventId());

		} catch (NotificationHandlingException ex) {
		    logger.warn("Notification discarded: Notification discarded for event {}: {}", notificationEvent.eventId(), ex.getMessage());
		} catch (Exception ex) {
		    logger.error("Unexpected notification error", ex);
		    throw ex;
		}
	}

}
