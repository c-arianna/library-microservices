package mentoring.acomi.userservice.application.projection;

import org.springframework.stereotype.Component;

import mentoring.acomi.userservice.application.repositories.UserViewRepository;

@Component
public class UserProjection extends AbstractUserProjection {

    public UserProjection(UserViewRepository repository) {
        super(repository);
    }
}