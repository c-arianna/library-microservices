package mentoring.acomi.userservice.application.repositories;

import java.util.List;
import java.util.Optional;

import mentoring.acomi.userservice.application.UserFilter;
import mentoring.acomi.userservice.application.view.UserView;

public interface UserViewQueryRepository {
	public Optional<UserView> findById(String id);
	public Optional<UserView> findByEmail(String email);
	public List<UserView> find(UserFilter filter);
	List<UserView> findWithoutCardNumber();
}
