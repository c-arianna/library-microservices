package mentoring.acomi.notificationservice.infrastructure.messaging;

import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

@Configuration
public class RabbitMQConfig {

	@Bean
	MessageConverter messageConverter() {
		return new JacksonJsonMessageConverter();
	}
	
	@Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);

        factory.setPrefetchCount(1);
		
		factory.setDefaultRequeueRejected(false);

        return factory;
    }
}