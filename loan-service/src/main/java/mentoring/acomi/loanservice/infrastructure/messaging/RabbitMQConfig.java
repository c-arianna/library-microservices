package mentoring.acomi.loanservice.infrastructure.messaging;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;

@Configuration
public class RabbitMQConfig {
	
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
		return new Declarables(BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.BOOK_RESERVED.toString()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.BOOK_RESERVATION_REJECTED.toString()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.BOOK_BORROWED.toString()),
				BindingBuilder.bind(queue).to(exchange).with(IntegrationEventTypes.BOOK_BORROW_REJECTED.toString()));
	}

	@Bean
	MessageConverter messageConverter() {
		return new JacksonJsonMessageConverter();
	}

}
