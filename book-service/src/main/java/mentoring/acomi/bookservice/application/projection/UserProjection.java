package mentoring.acomi.bookservice.application.projection;

import java.time.Instant;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.repositories.UserViewRepository;
import mentoring.acomi.bookservice.application.view.UserView;

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
		repository.unsubscribeUser(userId, occurredAt);
	}
		
	private UserView getUser(UserSubscriptionData subscriptionData) {
		return new UserView(subscriptionData.userId(), subscriptionData.email(), subscriptionData.name(), subscriptionData.lastname(), 
				subscriptionData.userIdentityProviderId(), subscriptionData.cardNumber(), subscriptionData.status());
	}
}