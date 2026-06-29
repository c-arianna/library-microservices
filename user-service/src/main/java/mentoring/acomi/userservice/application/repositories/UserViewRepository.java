package mentoring.acomi.userservice.application.repositories;

import java.time.Instant;

import mentoring.acomi.sharedlibrary.model.UserStatus;
import mentoring.acomi.userservice.application.view.UserView;

public interface UserViewRepository {
	public void add(UserView user, Instant createdAt);
	public void updateStatus(String id, UserStatus status, Instant updatedAt);
	public void deleteAll();
}
