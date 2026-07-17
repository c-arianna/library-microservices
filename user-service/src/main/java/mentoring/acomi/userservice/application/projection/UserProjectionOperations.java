package mentoring.acomi.userservice.application.projection;

import java.time.Instant;

import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;

public interface UserProjectionOperations {
	void subscribeUser(UserSubscribedIntegrationPayload payload, Instant occurredAt);
	void unsubscribeUser(UserIntegrationPayload payload, Instant occurredAt);
	void suspendUser(UserIntegrationPayload payload, Instant occurredAt);
	void unsuspendUser(UserIntegrationPayload payload, Instant occurredAt);
}
