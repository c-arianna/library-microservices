package mentoring.acomi.userservice.infrastructure.messaging.notifications;

import org.springframework.stereotype.Component;

import mentoring.acomi.userservice.application.errors.UserNotFound;
import mentoring.acomi.userservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.userservice.application.view.UserView;

@Component
public class UserNotificationService {

	private final UserViewQueryRepository userRepository;
	private final UserNotificationPublisher publisher;
	
	public UserNotificationService(UserViewQueryRepository userRepository, UserNotificationPublisher publisher) {
		this.userRepository = userRepository;
		this.publisher = publisher;
	}
	
	public void publishUserUpdated(String userId) {
        UserView user = userRepository.findById(userId).orElseThrow(() -> new UserNotFound("%s not found".formatted(userId)));
        publisher.publishUserUpdated(user);
    }
	
}