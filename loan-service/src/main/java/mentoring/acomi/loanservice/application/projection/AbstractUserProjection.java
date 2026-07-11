package mentoring.acomi.loanservice.application.projection;

import java.time.Instant;

import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;

public class AbstractUserProjection {

private final UserViewRepository repository;
	
	public AbstractUserProjection(UserViewRepository repository) {
		this.repository = repository;
	}
	
	@Transactional
	public void handleSubscribeUser(UserSubscribedIntegrationPayload payload, Instant occurreAt) {
		UserView user = new UserView(payload.userId(), payload.email(), payload.userIdentityProviderId(), payload.status());
		repository.add(user, occurreAt);
	}

	@Transactional
	public void handleUpdateUserStatus(UserIntegrationPayload payload, Instant occurredAt) {
		repository.updateStatus(payload.userId(), payload.status(), occurredAt);
	}
}
