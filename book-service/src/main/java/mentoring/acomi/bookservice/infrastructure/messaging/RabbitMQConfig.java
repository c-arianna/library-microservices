package mentoring.acomi.bookservice.infrastructure.messaging;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;

@Configuration
public class RabbitMQConfig {

	@Bean
	TopicExchange eventsExchange() {
		return new TopicExchange(MessagingTopology.EVENTS_EXCHANGE);
	}

	@Bean
	Queue bookQueue() {
		return new Queue(MessagingTopology.BOOK_QUEUE, true);
	}

	@Bean
	Declarables bookBindings(Queue queue, TopicExchange exchange) {
		return new Declarables(BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.LOAN_REQUESTED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.LOAN_CONFIRM_REQUESTED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.LOAN_CANCELED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.LOAN_RETURNED.getRoutingKey()));
	}

	@Bean
	MessageConverter messageConverter() {
		return new JacksonJsonMessageConverter();
	}
}