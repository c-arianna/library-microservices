package mentoring.acomi.loanservice.config;

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
	Queue loanQueue() {
		return new Queue(MessagingTopology.LOAN_QUEUE, true);
	}

	@Bean
	Declarables loanBindings(Queue queue, TopicExchange exchange) {
		return new Declarables(BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.BOOK_RESERVED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.BOOK_RESERVATION_REJECTED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.BOOK_BORROWED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.BOOK_BORROW_REJECTED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.USER_SUBSCRIBED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.USER_UNSUBSCRIBED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.USER_SUSPENDED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.USER_UNSUSPENDED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.LOAN_REQUESTED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.LOAN_FAILED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.LOAN_RESERVED.getRoutingKey()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.LOAN_CONFIRMED.getRoutingKey()));
	}

}
