package mentoring.acomi.loanservice.application.projection;

import java.time.Instant;

import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;

public interface UserProjectionOperations {
	void handleSubscribeUser(UserSubscribedIntegrationPayload payload, Instant occurreAt);
	void handleUpdateUserStatus(UserIntegrationPayload payload, Instant occurredAt);
}
