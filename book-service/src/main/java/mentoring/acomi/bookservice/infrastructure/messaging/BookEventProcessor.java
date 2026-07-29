package mentoring.acomi.bookservice.infrastructure.messaging;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.BookNotificationService;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookIntegrationRepository;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandlerRegistry;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@Component
public class BookEventProcessor {

	private final BookIntegrationRepository bookIntegrationRepository;
	private final BookEventRepository bookEventRepository;
	private final EventHandlerRegistry registry;
	private final BookNotificationService notificationService;
	
	private final Logger logger = LogManager.getLogger(BookEventProcessor.class);
	
	public BookEventProcessor(BookIntegrationRepository bookIntegrationRepository, BookEventRepository bookEventRepository, EventHandlerRegistry registry,
			BookNotificationService notificationService) {
		this.bookIntegrationRepository = bookIntegrationRepository;
		this.bookEventRepository = bookEventRepository;
		this.registry = registry;
		this.notificationService = notificationService;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processConsumerEvent(IntegrationEventEnvelope<?> event, LoanIntegrationPayload payload) {

		if (bookIntegrationRepository.exists(event.eventId(), AggregateType.LOAN.name())) {
			return;
		}

		try {
			bookIntegrationRepository.save(event);
		} catch (DataIntegrityViolationException e) {
			return;
		}
		
		handleEvent(event);
		bookEventRepository.markProcessed(event.eventId(), event.aggregateType());	
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processProducerEvent( IntegrationEventEnvelope<?> event) {
		
		if (bookEventRepository.existsEventProcessed(event.eventId(), AggregateType.BOOK.name())) {
			return;
		}
		
		Optional<ProjectionUpdateNotification> notification = handleEvent(event);
		
		if(notification.isPresent()) {
			notificationService.publishBookUpdated(notification.get().aggregateId());
		}
		
		notificationService.notifyAvailability(notification.get().aggregateId());
		
		bookEventRepository.markProcessed(event.eventId(), event.aggregateType());	
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
