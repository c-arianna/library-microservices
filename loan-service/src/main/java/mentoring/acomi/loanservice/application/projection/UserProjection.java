package mentoring.acomi.loanservice.application.projection;

import java.time.Instant;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;

@Component
public class UserProjection implements UserProjectionOperations {

	private final UserViewRepository repository;

	public UserProjection(UserViewRepository repository) {
		this.repository = repository;
	}

	@Override
	public void handleSubscribeUser(UserSubscribedIntegrationPayload payload, Instant occurreAt) {
		UserView user = new UserView(payload.userId(), payload.email(), payload.userIdentityProviderId(),
				payload.status());
		repository.add(user, occurreAt);
	}

	@Override
	public void handleUpdateUserStatus(UserIntegrationPayload payload, Instant occurredAt) {
		repository.updateStatus(payload.userId(), payload.status(), occurredAt);
	}

}
