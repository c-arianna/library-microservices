package mentoring.acomi.userservice.application.projection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.sharedlibrary.model.UserStatus;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;

@Component
public class UserProjection {

	private final UserViewRepository repository;

	public UserProjection(UserViewRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void subscribeUser(UserSubscribedIntegrationPayload payload) {
		repository.add(getUser(payload));
	}
	
	@Transactional
	public void unsubscribeUser(UserIntegrationPayload payload) {
		repository.updateStatus(payload.userId(), UserStatus.DISABLE);
	}
	
	@Transactional
	public void suspendUser(UserIntegrationPayload payload) {
		repository.updateStatus(payload.userId(), UserStatus.SUSPENDED);
	}
	
	@Transactional
	public void unsuspendUser(UserIntegrationPayload payload) {
		repository.updateStatus(payload.userId(), UserStatus.ACTIVE);
	}
	
	private UserView getUser(UserSubscribedIntegrationPayload payload) {
		return new UserView(payload.userId(), payload.email(), payload.name(), payload.lastname(), payload.userIdentityProviderId(), payload.status(), payload.role());
	}
}