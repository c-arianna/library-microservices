package mentoring.acomi.bookservice.infrastructure.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.services.BookEventService;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanIntegrationPayload;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;
import tools.jackson.databind.ObjectMapper;

@Component
public class BookIntegrationEventListener {

	private final ObjectMapper mapper;
	private final BookEventService service;

	private final Logger logger = LogManager.getLogger(BookIntegrationEventListener.class);

	public BookIntegrationEventListener(ObjectMapper mapper, BookEventService service) {
		this.mapper = mapper;
		this.service = service;
	}

	@RabbitListener(queues = MessagingTopology.BOOK_QUEUE)
	public void onEvent(IntegrationEventEnvelope<?> eventEnvelope) {
		try {
			LoanIntegrationPayload payload = mapper.convertValue(eventEnvelope.payload(), LoanIntegrationPayload.class);

			validatePayload(eventEnvelope, payload);

			service.handle(eventEnvelope.eventType(), payload);

		} catch (Exception e) {
			logger.error("Failed to process event type={}, eventId={}, aggregateId={}", eventEnvelope.eventType(),
					eventEnvelope.eventId(), eventEnvelope.aggregateId(), e);
			throw e;
		}
	}

	private void validatePayload(IntegrationEventEnvelope<?> eventEnvelope, LoanIntegrationPayload payload) {
		if (payload == null || payload.loanId() == null || payload.userId() == null || payload.isbn() == null) {
			throw new IllegalArgumentException("Invalid payload for event " + eventEnvelope.eventType());
		}
	}

}