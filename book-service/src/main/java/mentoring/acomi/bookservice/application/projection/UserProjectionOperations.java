package mentoring.acomi.bookservice.application.projection;

import java.time.Instant;

public interface UserProjectionOperations {
	void subscribeUser(UserSubscriptionData subscriptionData, Instant occurredAt);
	void unsubscribeUser(String userId, Instant occurredAt);
}
