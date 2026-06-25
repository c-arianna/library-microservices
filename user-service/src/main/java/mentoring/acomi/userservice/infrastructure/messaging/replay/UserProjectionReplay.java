package mentoring.acomi.userservice.infrastructure.messaging.replay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.userservice.application.projection.AbstractUserProjection;

@Component
public class UserProjectionReplay extends AbstractUserProjection {

	public UserProjectionReplay(@Qualifier("replayRepo") UserViewReplayRepository repository) {
		super(repository);
	}

}