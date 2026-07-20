package mentoring.acomi.loanservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mentoring.acomi.loanservice.infrastructure.messaging.replay.ReplayLoanHandlerFactory;
import mentoring.acomi.sharedcorelibrary.replay.ReplayEventHandlerRegistry;

@Configuration
public class ReplayConfiguration {

    @Bean
    ReplayEventHandlerRegistry replayEventHandlerRegistry(ReplayLoanHandlerFactory factory) {
        return new ReplayEventHandlerRegistry(factory.createHandlers());
    }
    
}