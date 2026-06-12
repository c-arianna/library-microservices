package mentoring.acomi.userservice.application.services;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;
import mentoring.acomi.userservice.application.aggregates.UserAggregate;
import mentoring.acomi.userservice.application.errors.InvalidUser;
import mentoring.acomi.userservice.application.errors.UserNotFound;
import mentoring.acomi.userservice.application.messaging.EventDispatcher;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.domain.errors.ApplicationConflict;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.model.Password;
import mentoring.acomi.userservice.domain.model.User;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.SuspendRequest;
import mentoring.acomi.userservice.infrastructure.dto.UnsubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.UserResponse;

@Service
public class UserService {

	private final UserViewRepository userViewRepository;
	private final UserEventRepository userEventRepository;
	private final PasswordEncoder passwordEncoder;
	private final EventDispatcher eventDispatcher;

	private final Logger logger = LogManager.getLogger(UserService.class);

	public UserService(UserViewRepository userViewRepository, UserEventRepository userEventRepository,
			PasswordEncoder passwordEncoder, EventDispatcher eventDispatcher) {
		this.userViewRepository = userViewRepository;
		this.userEventRepository = userEventRepository;
		this.passwordEncoder = passwordEncoder;
		this.eventDispatcher = eventDispatcher;
	}

	@Transactional
	public User subscribe(SubscribeRequest request) {

		String userId = UUID.randomUUID().toString();
		String email = request.email();

		if (userViewRepository.findByEmail(email).isPresent()) {
			throw new ApplicationConflict("USER_ALREADY_EXISTS", String.format("Email: %s", email));
		}

		UserAggregate aggregate = loadUser(userId);
		User user = getUser(userId, request);
		aggregate.subscribe(user);

		return user;

	}

	@Transactional
	public UserResponse unsubscribe(UnsubscribeRequest request) {

		String currentUserId = getLoggedUserId();

		UserAggregate aggregate = loadUser(currentUserId);
		aggregate.unsubscribe(request.reason());
		return new UserResponse(currentUserId, aggregate.email(), aggregate.role(), UserStatus.DISABLE);
	}

	@Transactional
	public UserResponse suspend(SuspendRequest request) {

		UserAggregate aggregate = loadUser(request.userId());

		String currentUserId = getLoggedUserId();

		aggregate.suspend(request.reason(), currentUserId);
		return new UserResponse(request.userId(), aggregate.email(), aggregate.role(), UserStatus.SUSPENDED);
	}

	@Transactional
	public UserResponse unsuspend(SuspendRequest request) {
		UserAggregate aggregate = loadUser(request.userId());

		String currentUserId = getLoggedUserId();

		aggregate.unsuspend(request.reason(), currentUserId);
		return new UserResponse(request.userId(), aggregate.email(), aggregate.role(), UserStatus.ACTIVE);
	}

	private String getLoggedUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null) {
			throw new InvalidUser("User not logged");
		}

		Jwt jwt = (Jwt) auth.getPrincipal();
		String email = jwt.getClaim("email");

		if (email == null) {
			throw new UserNotFound("Email not present in token");
		}

		UserView user = userViewRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFound(String.format("Email: %s", email)));

		return user.id();
	}

	private User getUser(String userId, SubscribeRequest request) {
		String hashedPassowrd = passwordEncoder.encode(request.password());
		return User.create(userId, request.email(), request.name(), request.lastname(), Password.hashed(hashedPassowrd),
				UserRole.READER);
	}

	private UserAggregate loadUser(String userId) {
		List<UserEvent> events = userEventRepository.loadStream(userId);
		Consumer<UserEvent> dispatch = event -> {
			userEventRepository.appendToStream(event);
			try {
				eventDispatcher.dispatch(event);
			} catch (Exception e) {
				logger.error("[Dispatch] error after event persistence, eventType={}", event.type(), e);
			}
		};

		return new UserAggregate(userId, dispatch, events);
	}

}
