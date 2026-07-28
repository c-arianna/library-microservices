package mentoring.acomi.bookservice.infrastructure.messaging.notifications;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerRegistry;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.error.NotificationHandlingException;

@Component
public class BookNotificationListener {

	private final NotificationHandlerRegistry registry;

	private final Logger logger = LogManager.getLogger(BookNotificationListener.class);
	
	public BookNotificationListener(NotificationHandlerRegistry registry) {
		this.registry = registry;
	}
	
	@RabbitListener(queues = MessagingTopology.BOOK_NOTIFICATION_QUEUE)
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
