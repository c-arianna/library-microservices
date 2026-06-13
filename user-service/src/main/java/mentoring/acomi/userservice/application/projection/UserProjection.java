package mentoring.acomi.userservice.application.projection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.sharedlibrary.model.UserStatus;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.UserSubscribedEvent;
import mentoring.acomi.userservice.domain.events.UserSuspendEvent;
import mentoring.acomi.userservice.domain.events.UserUnsubscribeEvent;
import mentoring.acomi.userservice.domain.events.UserUnsuspendedEvent;
import mentoring.acomi.userservice.domain.events.payload.UserSubscribedPayload;

@Component
public class UserProjection {

	private final UserViewRepository repository;

	public UserProjection(UserViewRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public void updateView(UserEvent event) {

		switch (event) {
			case UserSubscribedEvent e -> repository.add(getUser(e.payload()));
			case UserUnsubscribeEvent e -> repository.updateStatus(e.payload().userId(), UserStatus.DISABLE);
			case UserSuspendEvent e -> repository.updateStatus(e.payload().userId(), UserStatus.SUSPENDED);
			case UserUnsuspendedEvent e -> repository.updateStatus(e.payload().userId(), UserStatus.ACTIVE);
		}

	}

	private UserView getUser(UserSubscribedPayload payload) {
		return new UserView(payload.id(), payload.email(), payload.name(), payload.lastname(), payload.userIdentityProviderId(), payload.status(), payload.role());
	}
}