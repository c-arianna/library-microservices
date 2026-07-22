package mentoring.acomi.loanservice.application.projection;

import java.time.Instant;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@Component
public class UserProjection implements UserProjectionOperations {

	private final UserViewRepository repository;

	public UserProjection(UserViewRepository repository) {
		this.repository = repository;
	}

	@Override
	public void handleSubscribeUser(UserSubscribedIntegrationPayload payload, Instant occurreAt) {
		UserView user = new UserView(payload.userId(), payload.email(), payload.name(), payload.lastname(), payload.cardNumber(), 
				payload.userIdentityProviderId(), payload.status());
		repository.add(user, occurreAt);
	}

	@Override
	public void handleUpdateUserStatus(String userId, UserStatus status, Instant occurredAt) {
		repository.updateStatus(userId, status, occurredAt);
	}
	
	@Override
	public void assignCardNumber(String userId, String cardNumber, Instant occurredAt) {
	    repository.updateCardNumber(userId, cardNumber, occurredAt);
	}

}
