package mentoring.acomi.loanservice.application.projection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;

@Component
public class UserProjection {
	
	private final UserViewRepository repository;
	
	public UserProjection(UserViewRepository repository) {
		this.repository = repository;
	}
	
	@Transactional
	public void handleSubscribeUser(UserSubscribedIntegrationPayload payload) {
		UserView user = new UserView(payload.userId(), payload.email(), payload.status());
		repository.add(user);
	}

	@Transactional
	public void handleUpdateUserStatus(UserIntegrationPayload payload) {
		repository.updateStatus(payload.userId(), payload.status());
	}
	

}
