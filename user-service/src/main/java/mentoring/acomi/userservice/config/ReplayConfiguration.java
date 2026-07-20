package mentoring.acomi.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mentoring.acomi.sharedcorelibrary.replay.ReplayEventHandlerRegistry;
import mentoring.acomi.userservice.infrastructure.messaging.replay.ReplayUserHandlerFactory;

@Configuration
public class ReplayConfiguration {

    @Bean
    ReplayEventHandlerRegistry replayEventHandlerRegistry(ReplayUserHandlerFactory factory) {
        return new ReplayEventHandlerRegistry(factory.createHandlers());
    }
}
