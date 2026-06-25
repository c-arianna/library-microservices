package mentoring.acomi.loanservice.application.repositories;

import java.util.Optional;

import mentoring.acomi.loanservice.application.view.UserView;

public interface UserViewQueryRepository {
	public Optional<UserView> findById(String id);
	public Optional<UserView> findByEmail(String email);
}
