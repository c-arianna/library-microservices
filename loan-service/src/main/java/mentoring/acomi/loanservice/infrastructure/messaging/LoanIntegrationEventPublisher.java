package mentoring.acomi.loanservice.infrastructure.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;

@Component
public class LoanIntegrationEventPublisher {
	
	private final RabbitTemplate rabbitTemplate;
    private final LoanIntegrationEventMapper mapper;
    
    public LoanIntegrationEventPublisher(RabbitTemplate rabbitTemplate, LoanIntegrationEventMapper mapper) {
    	this.rabbitTemplate = rabbitTemplate;
    	this.mapper = mapper;
    }
    
    private void publish(IntegrationEventEnvelope<?> eventEnvelope) {
		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, eventEnvelope.eventType().toString(), eventEnvelope);
	}

	public void dispatch(LoanEvent event) {
		IntegrationEventEnvelope<?> eventToPublish = mapper.map(event);
		publish(eventToPublish);
	}
}
