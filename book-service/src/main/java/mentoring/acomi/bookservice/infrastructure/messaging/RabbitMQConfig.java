package mentoring.acomi.bookservice.infrastructure.messaging;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;

@Configuration
public class RabbitMQConfig {

	private static final String LOAN_RETURNED = "loan.returned";
	private static final String LOAN_CANCELED = "loan.canceled";
	private static final String LOAN_CONFIRMED = "loan.confirmed";
	private static final String LOAN_REQUESTED = "loan.requested";

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
		return new Declarables(BindingBuilder.bind(queue).to(exchange).with(LOAN_REQUESTED),
				BindingBuilder.bind(queue).to(exchange).with(LOAN_CONFIRMED),
				BindingBuilder.bind(queue).to(exchange).with(LOAN_CANCELED),
				BindingBuilder.bind(queue).to(exchange).with(LOAN_RETURNED));
	}

	@Bean
	MessageConverter messageConverter() {
		return new JacksonJsonMessageConverter();
	}
}