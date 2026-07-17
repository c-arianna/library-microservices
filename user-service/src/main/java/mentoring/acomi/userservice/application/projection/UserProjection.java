package mentoring.acomi.userservice.application.projection;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;

@Component
public class UserProjection implements UserProjectionOperations {

	private final UserViewRepository repository;

	public UserProjection(UserViewRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void subscribeUser(UserSubscribedIntegrationPayload payload, Instant occurredAt) {
		repository.add(getUser(payload), occurredAt);
	}
	
	@Transactional
	public void unsubscribeUser(UserIntegrationPayload payload, Instant occurredAt) {
		repository.updateStatus(payload.userId(), UserStatus.DISABLED, occurredAt);
	}
	
	@Transactional
	public void suspendUser(UserIntegrationPayload payload, Instant occurredAt) {
		repository.updateStatus(payload.userId(), UserStatus.SUSPENDED, occurredAt);
	}
	
	@Transactional
	public void unsuspendUser(UserIntegrationPayload payload, Instant occurredAt) {
		repository.updateStatus(payload.userId(), UserStatus.ACTIVE, occurredAt);
	}
	
	private UserView getUser(UserSubscribedIntegrationPayload payload) {
		return new UserView(payload.userId(), payload.email(), payload.name(), payload.lastname(), payload.userIdentityProviderId(), payload.status(), payload.role());
	}
}