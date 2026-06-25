package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.AbstractUserProjection;

@Component
public class UserProjectionReplay extends AbstractUserProjection{

	public UserProjectionReplay(@Qualifier("userReplayRepo") UserViewReplayRepository repository) {
		super(repository);
	}

}
