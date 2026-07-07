package mentoring.acomi.bookservice.integration.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class FailingListener {

    @RabbitListener(queues = "test.queue")
    public void consume(String payload) {
    	throw new RuntimeException("Error");
    }
}