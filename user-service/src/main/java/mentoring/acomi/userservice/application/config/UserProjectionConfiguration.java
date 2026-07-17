package mentoring.acomi.userservice.application.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mentoring.acomi.userservice.application.projection.UserProjection;
import mentoring.acomi.userservice.application.projection.UserProjectionOperations;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;

@Configuration
public class UserProjectionConfiguration {

    @Bean("liveUserProjection")
    UserProjectionOperations liveUserProjection(UserViewRepository repository) {
        return new UserProjection(repository);
    }

    @Bean("replayUserProjection")
    UserProjectionOperations replayuserProjection(@Qualifier("replayRepo") UserViewRepository repository) {
        return new UserProjection(repository);
    }

}
