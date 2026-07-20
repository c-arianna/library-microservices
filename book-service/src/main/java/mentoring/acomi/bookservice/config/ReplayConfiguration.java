package mentoring.acomi.bookservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mentoring.acomi.bookservice.infrastructure.messaging.replay.ReplayBookHandlerFactory;
import mentoring.acomi.sharedcorelibrary.replay.ReplayEventHandlerRegistry;

@Configuration
public class ReplayConfiguration {

    @Bean
    ReplayEventHandlerRegistry replayEventHandlerRegistry(ReplayBookHandlerFactory factory) {
        return new ReplayEventHandlerRegistry(factory.createHandlers());
    }
}
