package mentoring.acomi.assistantservice.application.projection;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.repositories.UserViewRepository;
import mentoring.acomi.assistantservice.application.view.UserView;

@Component
public class UserProjection extends AbstractProjectionOperations<UserView> {

    public UserProjection(UserViewRepository repository) {
        super(repository);
    }

}
