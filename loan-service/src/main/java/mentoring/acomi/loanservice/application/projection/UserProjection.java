package mentoring.acomi.loanservice.application.projection;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.repositories.UserViewRepository;

@Component
public class UserProjection extends AbstractUserProjection {
	
	public UserProjection(UserViewRepository repository) {
		super(repository);
	}
	
}
