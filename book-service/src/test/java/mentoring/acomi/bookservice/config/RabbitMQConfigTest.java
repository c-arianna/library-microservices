package mentoring.acomi.bookservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

@EnableAutoConfiguration
@TestConfiguration
public class RabbitMQConfigTest {

	@Bean
	TopicExchange eventsExchange() {
		return new TopicExchange(MessagingTopology.EVENTS_EXCHANGE);
	}

	@Bean
	TopicExchange notificationsExchange() {
		return new TopicExchange(MessagingTopology.NOTIFICATIONS_EXCHANGE);
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
	Queue notificationQueue() {
		return new Queue(MessagingTopology.NOTIFICATION_QUEUE, true);
	}
	
	@Bean
	Queue bookNotificationQueue() {
		return new Queue(MessagingTopology.BOOK_NOTIFICATION_QUEUE, true);
	}
	
	@Bean
    Declarables bookBindings(Queue bookQueue, TopicExchange eventsExchange) {
        return new Declarables(
            BindingBuilder.bind(bookQueue).to(eventsExchange)
                .with(IntegrationEventTypes.LOAN_REQUESTED.getRoutingKey()),

            BindingBuilder.bind(bookQueue).to(eventsExchange)
                .with(IntegrationEventTypes.LOAN_CONFIRM_REQUESTED.getRoutingKey()),

            BindingBuilder.bind(bookQueue).to(eventsExchange)
                .with(IntegrationEventTypes.LOAN_CANCELED.getRoutingKey()),

            BindingBuilder.bind(bookQueue).to(eventsExchange)
                .with(IntegrationEventTypes.LOAN_RETURNED.getRoutingKey()),

            BindingBuilder.bind(bookQueue).to(eventsExchange)
                .with(IntegrationEventTypes.BOOK_REGISTERED.getRoutingKey()),
                
            BindingBuilder.bind(bookQueue).to(eventsExchange)
                .with(IntegrationEventTypes.BOOK_COPIES_UPDATED.getRoutingKey()),
                
            BindingBuilder.bind(bookQueue).to(eventsExchange)
                .with(IntegrationEventTypes.BOOK_RESERVED.getRoutingKey()),
                
            BindingBuilder.bind(bookQueue).to(eventsExchange)
                .with(IntegrationEventTypes.BOOK_BORROWED.getRoutingKey())
        );
    }
	
	@Bean
    Declarables notificationBindings(Queue notificationQueue, TopicExchange notificationsExchange) {
        return new Declarables(
            BindingBuilder.bind(notificationQueue).to(notificationsExchange)
                .with(NotificationEventType.BOOK_UPDATED.getRoutingKey()));
    }
	
	@Bean
    Declarables bookNotificationBindings(Queue bookNotificationQueue, TopicExchange notificationsExchange) {
        return new Declarables(
            BindingBuilder.bind(bookNotificationQueue).to(notificationsExchange)
                .with(NotificationEventType.BOOK_SUBSCRIPTION_NOTIFIED.getRoutingKey()));
    }
	
	@Bean
	FanoutExchange dlx() {
	    return new FanoutExchange("dlq-test.dlx");
	}

	@Bean
	Queue testQueue() {
	    return QueueBuilder.durable("test.queue")
	            .deadLetterExchange("dlq-test.dlx")
	            .deadLetterRoutingKey("test.routing.key")
	            .build();
	}

	@Bean
	Queue deadLetterQueue() {
	    return QueueBuilder.durable("test.dlq").build();
	}
	
	@Bean
	Binding testQueueBinding(Queue testQueue, TopicExchange eventsExchange) {

	    return BindingBuilder.bind(testQueue)
	            .to(eventsExchange)
	            .with("test.routing.key");
	}

	@Bean
	Binding deadLetterBinding(Queue deadLetterQueue, FanoutExchange dlx) {
	    return BindingBuilder.bind(deadLetterQueue)
	            .to(dlx);
	}

}
