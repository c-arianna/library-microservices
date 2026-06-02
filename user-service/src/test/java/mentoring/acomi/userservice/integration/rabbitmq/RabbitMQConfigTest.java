package mentoring.acomi.userservice.integration.rabbitmq;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import mentoring.acomi.sharedlibrary.integration.messaging.MessagingTopology;

@TestConfiguration
public class RabbitMQConfigTest {

	@Bean
	TopicExchange eventsExchange() {
		return new TopicExchange(MessagingTopology.EVENTS_EXCHANGE);
	}

}
