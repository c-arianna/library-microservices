package mentoring.acomi.loanservice.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

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
	Queue loanQueue() {
		return new Queue(MessagingTopology.LOAN_QUEUE, true);
	}
	
	@Bean
	Queue replayQueue() {
		return new Queue(MessagingTopology.REPLAY_LOAN_QUEUE, true);
	}

	@Bean
	Queue notificationQueue() {
		return new Queue(MessagingTopology.NOTIFICATION_QUEUE, true);
	}
	
	@Bean
	Declarables loanBindings(Queue loanQueue, TopicExchange eventsExchange) {
		return new Declarables(BindingBuilder.bind(loanQueue).to(eventsExchange).with(IntegrationEventTypes.BOOK_RESERVED.getRoutingKey()),
				BindingBuilder.bind(loanQueue).to(eventsExchange).with(IntegrationEventTypes.BOOK_RESERVATION_REJECTED.getRoutingKey()),
				BindingBuilder.bind(loanQueue).to(eventsExchange).with(IntegrationEventTypes.BOOK_BORROWED.getRoutingKey()),
				BindingBuilder.bind(loanQueue).to(eventsExchange).with(IntegrationEventTypes.BOOK_BORROW_REJECTED.getRoutingKey()),
				BindingBuilder.bind(loanQueue).to(eventsExchange).with(IntegrationEventTypes.BOOK_REGISTERED.getRoutingKey()),
				BindingBuilder.bind(loanQueue).to(eventsExchange).with(IntegrationEventTypes.USER_SUBSCRIBED.getRoutingKey()),
				BindingBuilder.bind(loanQueue).to(eventsExchange).with(IntegrationEventTypes.USER_UNSUBSCRIBED.getRoutingKey()),
				BindingBuilder.bind(loanQueue).to(eventsExchange).with(IntegrationEventTypes.USER_SUSPENDED.getRoutingKey()),
				BindingBuilder.bind(loanQueue).to(eventsExchange).with(IntegrationEventTypes.USER_UNSUSPENDED.getRoutingKey()),
				BindingBuilder.bind(loanQueue).to(eventsExchange).with(IntegrationEventTypes.LOAN_REQUESTED.getRoutingKey()),
				BindingBuilder.bind(loanQueue).to(eventsExchange).with(IntegrationEventTypes.LOAN_FAILED.getRoutingKey()),
				BindingBuilder.bind(loanQueue).to(eventsExchange).with(IntegrationEventTypes.LOAN_RESERVED.getRoutingKey()),
				BindingBuilder.bind(loanQueue).to(eventsExchange).with(IntegrationEventTypes.LOAN_CONFIRMED.getRoutingKey()));
	}
	
	@Bean
    Declarables notificationBindings(Queue notificationQueue, TopicExchange notificationsExchange) {
        return new Declarables(
            BindingBuilder.bind(notificationQueue).to(notificationsExchange)
                .with(NotificationEventType.LOAN_UPDATED.getRoutingKey()));
    }

}
