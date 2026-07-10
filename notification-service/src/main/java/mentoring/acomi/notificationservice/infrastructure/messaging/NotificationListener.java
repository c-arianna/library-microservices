package mentoring.acomi.notificationservice.infrastructure.messaging;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
import mentoring.acomi.notificationservice.infrastructure.messaging.handlers.NotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

@Component
public class NotificationListener {
	
	private final Logger logger = LogManager.getLogger(NotificationListener.class);
	
	private final Map<NotificationEventType, List<NotificationHandler>> handlers;
	
	public NotificationListener(List<NotificationHandler> handlers) {
		this.handlers = handlers.stream().collect(Collectors.groupingBy(NotificationHandler::eventType));
	}
	
	@RabbitListener(queues = MessagingTopology.NOTIFICATION_QUEUE)
	public void onEvent(NotificationEventEnvelope<?> notificationEvent) {
		NotificationEventType eventType = notificationEvent.eventType();
		
		logger.info("Processing event {} ({})", notificationEvent.eventId(), eventType);
		
		List<NotificationHandler> eventHandlers = handlers.get(notificationEvent.eventType());
		
		if (eventHandlers  == null) {
			 logger.warn("No handler found for event {} ({})", notificationEvent.eventId(), eventType);
			 return;
		}
		
		try {
			eventHandlers.stream().filter(h -> h.accepts(notificationEvent)).findFirst()
			.ifPresentOrElse(h -> h.handleEvent(notificationEvent), () -> logger.warn("No handler found for event {} with schemaVersion {}",
	                notificationEvent.eventType(), notificationEvent.schemaVersion()
	            )
	        );
		} catch (NotificationHandlingException ex) {
		    logger.warn("Notification discarded: Notification discarded for event {}: {}", notificationEvent.eventId(), ex.getMessage());
		} catch (Exception ex) {
		    logger.error("Unexpected notification error", ex);
		    throw ex;
		}
	}
	
}
