package mentoring.acomi.bookservice.infrastructure.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;

@Component
public class BookIntegrationEventPublisher {

	private final RabbitTemplate rabbitTemplate;
	private final BookIntegrationEventMapper mapper;
	
	public BookIntegrationEventPublisher(RabbitTemplate rabbitTemplate, BookIntegrationEventMapper mapper) {
		this.rabbitTemplate = rabbitTemplate;
		this.mapper = mapper;
	}

	private void publish(IntegrationEventEnvelope<?> eventEnvelope) {
		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, eventEnvelope.eventType().toString(), eventEnvelope);
	}

	public void dispatch(BookEvent event) {
		IntegrationEventEnvelope<?> eventToPublish = mapper.map(event);
		publish(eventToPublish);
	}
}