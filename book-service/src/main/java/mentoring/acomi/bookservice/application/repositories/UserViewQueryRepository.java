package mentoring.acomi.bookservice.application.repositories;

import java.util.Optional;

import mentoring.acomi.bookservice.application.view.UserView;

public interface UserViewQueryRepository {
	Optional<UserView> findById(String id);
	Optional<UserView> findNotDisabledUserByEmail(String email);
}
