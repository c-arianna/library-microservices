package mentoring.acomi.userservice.application.repositories;

import java.util.Optional;

import mentoring.acomi.userservice.application.view.UserView;

public interface UserViewRepository {
	public void add(UserView user);
	public Optional<UserView> findById(String id);
	public Optional<UserView> findByEmail(String email);
}
