package mentoring.acomi.userservice.application.projection;

import java.time.Instant;

public interface UserProjectionOperations {
	void subscribeUser(UserSubscriptionData subscriptionData, Instant occurredAt);
	void unsubscribeUser(String userId, Instant occurredAt);
	void suspendUser(String userId, Instant occurredAt);
	void unsuspendUser(String userId, Instant occurredAt);
	void assignCardNumber(String userId, String cardNumber, Instant occurredAt);
}
