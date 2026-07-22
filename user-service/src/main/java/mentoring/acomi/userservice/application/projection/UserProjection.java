package mentoring.acomi.userservice.application.projection;

import java.time.Instant;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.view.UserView;

@Component
public class UserProjection implements UserProjectionOperations {

	private final UserViewRepository repository;

	public UserProjection(UserViewRepository repository) {
		this.repository = repository;
	}

	@Override
	public void subscribeUser(UserSubscriptionData subscriptionData, Instant occurredAt) {
		repository.add(getUser(subscriptionData), occurredAt);
	}
	
	@Override
	public void unsubscribeUser(String userId, Instant occurredAt) {
		repository.updateStatus(userId, UserStatus.DISABLED, occurredAt);
	}
	
	@Override
	public void suspendUser(String userId, Instant occurredAt) {
		repository.updateStatus(userId, UserStatus.SUSPENDED, occurredAt);
	}
	
	@Override
	public void unsuspendUser(String userId, Instant occurredAt) {
		repository.updateStatus(userId, UserStatus.ACTIVE, occurredAt);
	}
	
	@Override
	public void assignCardNumber(String userId, String cardNumber, Instant occurredAt) {
	    repository.updateCardNumber(userId, cardNumber, occurredAt);
	}
	
	private UserView getUser(UserSubscriptionData subscriptionData) {
		return new UserView(subscriptionData.userId(), subscriptionData.email(), subscriptionData.name(), subscriptionData.lastname(), 
				subscriptionData.userIdentityProviderId(), subscriptionData.cardNumber(), subscriptionData.status(), 
				subscriptionData.role());
	}
}