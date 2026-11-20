package mentoring.acomi.assistantservice.application.repositories;

import java.util.Optional;

import mentoring.acomi.assistantservice.application.view.UserView;

public interface UserViewRepository extends ViewRepository<UserView>{
	Optional<UserView> findByUserIdentityProviderId(String identityProviderId);
}
