package mentoring.acomi.bookservice.application.repositories;

import java.time.Instant;

import mentoring.acomi.bookservice.application.view.UserView;

public interface UserViewRepository {
	void add(UserView user, Instant createdAt);
	void unsubscribeUser(String userId, Instant occurredAt);
	void deleteAll();
}
