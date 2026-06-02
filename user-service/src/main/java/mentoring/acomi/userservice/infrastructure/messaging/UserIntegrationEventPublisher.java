package mentoring.acomi.userservice.infrastructure.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.userservice.domain.events.UserEvent;

@Component
public class UserIntegrationEventPublisher {

	private final RabbitTemplate rabbitTemplate;
	private final UserIntegrationEventMapper mapper;
	
	public UserIntegrationEventPublisher(RabbitTemplate rabbitTemplate, UserIntegrationEventMapper mapper) {
		this.rabbitTemplate = rabbitTemplate;
		this.mapper = mapper;
	}

	private void publish(IntegrationEventEnvelope<?> eventEnvelope) {
		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, eventEnvelope.eventType().getRoutingKey(), eventEnvelope);
	}

	public void dispatch(UserEvent event) {
		IntegrationEventEnvelope<?> eventToPublish = mapper.map(event);
		publish(eventToPublish);
	}
}