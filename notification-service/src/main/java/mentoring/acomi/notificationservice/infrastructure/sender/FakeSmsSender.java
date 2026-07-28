package mentoring.acomi.notificationservice.infrastructure.sender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import mentoring.acomi.notificationservice.application.sender.SmsSender;

@Component
public class FakeSmsSender implements SmsSender {

	private final Logger logger = LogManager.getLogger(FakeSmsSender.class);
	
    @Override
    public void send(String phoneNumber, String message) {
    	logger.info("[FAKE SMS] to={} message={}", phoneNumber, message);
    }
    
}
