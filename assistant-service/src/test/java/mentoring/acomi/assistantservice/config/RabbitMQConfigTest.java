package mentoring.acomi.assistantservice.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

@EnableAutoConfiguration
@TestConfiguration
public class RabbitMQConfigTest {

	@Bean
	TopicExchange notificationsExchange() {
		return new TopicExchange(MessagingTopology.NOTIFICATIONS_EXCHANGE);
	}

	@Bean
	Queue assistantQueue() {
		return new Queue(MessagingTopology.ASSISTANT_QUEUE, true);
	}

	@Bean
	Declarables assistantBindings(Queue assistantQueue, TopicExchange notificationsExchange) {
		return new Declarables(
				BindingBuilder.bind(assistantQueue).to(notificationsExchange)
				.with(NotificationEventType.BOOK_UPDATED.getRoutingKey()),
				
				BindingBuilder.bind(assistantQueue).to(notificationsExchange)
				.with(NotificationEventType.LOAN_UPDATED.getRoutingKey()),
				
				BindingBuilder.bind(assistantQueue).to(notificationsExchange)
				.with(NotificationEventType.USER_UPDATED.getRoutingKey())				
				
				);
	}
	
}
