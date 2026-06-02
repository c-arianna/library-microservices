package mentoring.acomi.loanservice.application.repositories;

import java.util.Optional;

import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.sharedlibrary.model.UserStatus;

public interface UserViewRepository {
	public void add(UserView user);
	public Optional<UserView> findById(String id);
	public void updateStatus(String id, UserStatus status);
}
