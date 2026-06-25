package mentoring.acomi.userservice.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;

@TestConfiguration
public class RabbitMQConfigTest {

	@Bean
	TopicExchange eventsExchange() {
		return new TopicExchange(MessagingTopology.EVENTS_EXCHANGE);
	}
	
	@Bean
	Queue userQueue() {
		return new Queue(MessagingTopology.USER_QUEUE, true);
	}
	
	@Bean
	Queue replayQueue() {
		return new Queue(MessagingTopology.REPLAY_USER_QUEUE, true);
	}
	
	@Bean
	Declarables loanBindings(Queue userQueue, TopicExchange exchange) {
		return new Declarables(BindingBuilder.bind(userQueue).to(exchange).with(IntegrationEventTypes.USER_SUBSCRIBED.getRoutingKey()),
				BindingBuilder.bind(userQueue).to(exchange).with(IntegrationEventTypes.USER_UNSUBSCRIBED.getRoutingKey()),
				BindingBuilder.bind(userQueue).to(exchange).with(IntegrationEventTypes.USER_SUSPENDED.getRoutingKey()),
				BindingBuilder.bind(userQueue).to(exchange).with(IntegrationEventTypes.USER_UNSUSPENDED.getRoutingKey()));
	}


}
