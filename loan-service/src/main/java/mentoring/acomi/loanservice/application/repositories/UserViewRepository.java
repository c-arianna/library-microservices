package mentoring.acomi.loanservice.application.repositories;

import java.time.Instant;

import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.sharedlibrary.model.UserStatus;

public interface UserViewRepository {
	public void add(UserView user, Instant createdAt);
	public void updateStatus(String id, UserStatus status, Instant updatedAt);
}
