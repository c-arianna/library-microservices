package mentoring.acomi.sharedcodelibrary.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.Validator;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandlerRegistry;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerRegistry;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SharedLibraryCoreAutoConfiguration {

    @Bean
    EventPayloadMapper eventPayloadMapper(ObjectMapper objectMapper, Validator validator) {
        return new EventPayloadMapper(objectMapper, validator);
    }
    
    @Bean
    EventHandlerRegistry eventHandlerRegistry(List<EventHandler> handlers) {
    	return new EventHandlerRegistry(handlers);    	
    }
    
    @Bean
    NotificationHandlerRegistry notificationHandlerRegistry(List<NotificationHandler> handlers) {
    	return new NotificationHandlerRegistry(handlers);
    }
    
}