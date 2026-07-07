package mentoring.acomi.bookservice.integration.rabbitmq;

import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import mentoring.acomi.bookservice.config.RabbitMQConfigTest;
import mentoring.acomi.bookservice.infrastructure.messaging.RabbitMQConfig;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;

@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RabbitMQConfig.class,
        RabbitMQConfigTest.class,
        FailingListener.class
})
public class RabbitMqDlqIntegrationTest {

	@Container
	private static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");
		
	@Autowired
	private RabbitListenerEndpointRegistry registry;
	
	@DynamicPropertySource
	static void rabbitProps(DynamicPropertyRegistry registry) {
		registry.add("spring.rabbitmq.host", rabbit::getHost);
		registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
		registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
		registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
	}
	
	@Autowired
	RabbitTemplate rabbitTemplate;

	@AfterEach
	void stopListeners() {
	    registry.stop();
	}
	
	@Test
	void shouldMoveMessageToDlqWhenConsumerFails() {
		
	    rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, "test.routing.key", "test");
	    
	    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
	                Object dlqMessage = rabbitTemplate.receiveAndConvert("test.dlq");
	                Assertions.assertNotNull(dlqMessage);
	    });
	    
	    
	}
}
