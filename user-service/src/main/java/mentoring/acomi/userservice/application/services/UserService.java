package mentoring.acomi.userservice.application.services;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.userservice.application.aggregates.UserAggregate;
import mentoring.acomi.userservice.application.eventhandler.EventDispatcher;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.security.PasswordHasher;
import mentoring.acomi.userservice.application.security.TokenService;
import mentoring.acomi.userservice.domain.errors.ApplicationConflict;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.model.Password;
import mentoring.acomi.userservice.domain.model.User;
import mentoring.acomi.userservice.domain.model.UserRole;
import mentoring.acomi.userservice.domain.model.UserStatus;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeResponse;
import mentoring.acomi.userservice.infrastructure.dto.SuspendRequest;
import mentoring.acomi.userservice.infrastructure.dto.UnsubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.UserResponse;

@Service
public class UserService {

	private final UserViewRepository userViewRepository;
	private final UserEventRepository userEventRepository;
	private final PasswordHasher passwordHasher;
	private final EventDispatcher eventDispatcher;
	private final TokenService tokenService;
	
	private final Logger logger = LogManager.getLogger(UserService.class);

	public UserService(UserViewRepository userViewRepository, UserEventRepository userEventRepository,
			PasswordHasher passwordHasher, EventDispatcher eventDispatcher, TokenService tokenService) {
		this.userViewRepository = userViewRepository;
		this.userEventRepository = userEventRepository;
		this.passwordHasher = passwordHasher;
		this.eventDispatcher = eventDispatcher;
		this.tokenService = tokenService;
	}

	@Transactional
	public SubscribeResponse subscribe(SubscribeRequest request) {

		String userId = UUID.randomUUID().toString();
		String email = request.email();

		if (userViewRepository.findByEmail(email).isPresent()) {
			throw new ApplicationConflict("USER_ALREADY_EXISTS", String.format("Email: %s", email));
		}

		UserAggregate aggregate = loadUser(userId);
		User user = getUser(userId, request);
		aggregate.subscribe(user);

		String accessToken = tokenService.generateAccessToken(user.getId(), user.getEmail().getValue(), user.getRole());
		String refreshToken = tokenService.generateRefreshToken(user.getId());
		
		return new SubscribeResponse(userId, email, accessToken, refreshToken);

	}

	@Transactional
	public UserResponse unsubscribe(UnsubscribeRequest request) {
		UserAggregate aggregate = loadUser(request.userId());
		aggregate.unsubscribe(request.reason());
		return new UserResponse(request.userId(), aggregate.email(), aggregate.role(), UserStatus.DISABLE);
	}
	
	@Transactional
	public UserResponse suspend(SuspendRequest request) {
		UserAggregate aggregate = loadUser(request.userId());
		aggregate.suspend(request.reason(), request.suspendedBy());
		return new UserResponse(request.userId(), aggregate.email(), aggregate.role(), UserStatus.SUSPENDED);
	}
	
	public UserResponse unsuspend(SuspendRequest request) {
		UserAggregate aggregate = loadUser(request.userId());
		aggregate.unsuspend(request.reason(), request.suspendedBy());
		return new UserResponse(request.userId(), aggregate.email(), aggregate.role(), UserStatus.ACTIVE);
	}
	
	
	private User getUser(String userId, SubscribeRequest request) {
		return User.create(userId, request.email(), request.name(), request.lastname(),
				Password.create(request.password(), passwordHasher), UserRole.READER);
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
