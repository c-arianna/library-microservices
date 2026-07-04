package mentoring.acomi.bookservice.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;

@TestConfiguration
public class RabbitMQConfigTest {

	@Bean
	TopicExchange eventsExchange() {
		return new TopicExchange(MessagingTopology.EVENTS_EXCHANGE);
	}

	@Bean
	Queue bookQueue() {
		return new Queue(MessagingTopology.BOOK_QUEUE, true);
	}
	
	@Bean
	Queue replayQueue() {
		return new Queue(MessagingTopology.REPLAY_BOOK_QUEUE, true);
	}

	@Bean
    Declarables bookBindings(Queue bookQueue, TopicExchange exchange) {
        return new Declarables(
            BindingBuilder.bind(bookQueue).to(exchange)
                .with(IntegrationEventTypes.LOAN_REQUESTED.getRoutingKey()),

            BindingBuilder.bind(bookQueue).to(exchange)
                .with(IntegrationEventTypes.LOAN_CONFIRM_REQUESTED.getRoutingKey()),

            BindingBuilder.bind(bookQueue).to(exchange)
                .with(IntegrationEventTypes.LOAN_CANCELED.getRoutingKey()),

            BindingBuilder.bind(bookQueue).to(exchange)
                .with(IntegrationEventTypes.LOAN_RETURNED.getRoutingKey()),

            BindingBuilder.bind(bookQueue).to(exchange)
                .with(IntegrationEventTypes.BOOK_REGISTERED.getRoutingKey()),
                
            BindingBuilder.bind(bookQueue).to(exchange)
                .with(IntegrationEventTypes.BOOK_COPIES_UPDATED.getRoutingKey()),
                
            BindingBuilder.bind(bookQueue).to(exchange)
                .with(IntegrationEventTypes.BOOK_RESERVED.getRoutingKey()),
                
            BindingBuilder.bind(bookQueue).to(exchange)
                .with(IntegrationEventTypes.BOOK_BORROWED.getRoutingKey())
        );
    }

}
