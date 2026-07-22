package mentoring.acomi.loanservice.application.projection;

import java.time.Instant;

import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

public interface UserProjectionOperations {
	void handleSubscribeUser(UserSubscribedIntegrationPayload payload, Instant occurreAt);
	void handleUpdateUserStatus(String userId, UserStatus status, Instant occurredAt);
	void assignCardNumber(String userId, String cardNumber, Instant occurredAt);
}
