package mentoring.acomi.sharedcodelibrary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.Validator;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SharedLibraryCoreAutoConfiguration {

    @Bean
    EventPayloadMapper eventPayloadMapper(ObjectMapper objectMapper, Validator validator) {
        return new EventPayloadMapper(objectMapper, validator);
    }
    
}