package mentoring.acomi.userservice.application.repositories;

import java.time.Instant;

import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.view.UserView;

public interface UserViewRepository {
	public void add(UserView user, Instant createdAt);
	public void updateStatus(String id, UserStatus status, Instant updatedAt);
	public void unsubscribeUser(String userId, Instant occurredAt);
	public void deleteAllReaderUsers();
	public void updateCardNumber(String userId, String cardNumber, Instant occurredAt);
	public void deleteByUserIdentityProviderId(String userIdentityProviderId);
}