package mentoring.acomi.userservice.application.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;
import mentoring.acomi.userservice.application.aggregates.UserAggregate;
import mentoring.acomi.userservice.application.errors.InvalidUser;
import mentoring.acomi.userservice.application.errors.InvalidUserData;
import mentoring.acomi.userservice.application.errors.UserCreationError;
import mentoring.acomi.userservice.application.errors.UserNotFound;
import mentoring.acomi.userservice.application.messaging.EventDispatcher;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.userservice.application.sso.IdentityProviderService;
import mentoring.acomi.userservice.application.sso.ProviderUserCreated;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.domain.errors.ApplicationConflict;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.model.User;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.SuspendRequest;
import mentoring.acomi.userservice.infrastructure.dto.UnsubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.UserResponse;
import mentoring.acomi.userservice.infrastructure.dto.UserSubscribedResponse;
import mentoring.acomi.userservice.infrastructure.dto.UsersResponse;
import mentoring.acomi.userservice.infrastructure.sso.keycloak.errors.KeycloakException;

@Service
public class UserService {

	private final UserViewQueryRepository userViewRepository;
	private final UserEventRepository userEventRepository;
	private final EventDispatcher eventDispatcher;
	private final IdentityProviderService identityProviderService;
	
	private final Logger logger = LogManager.getLogger(UserService.class);

	public UserService(UserViewQueryRepository userViewRepository, UserEventRepository userEventRepository,
			EventDispatcher eventDispatcher, IdentityProviderService identityProviderService) {
		this.userViewRepository = userViewRepository;
		this.userEventRepository = userEventRepository;
		this.eventDispatcher = eventDispatcher;
		this.identityProviderService = identityProviderService;
	}

	@Transactional
	public UserSubscribedResponse subscribe(SubscribeRequest request, String role) {

		String userId = UUID.randomUUID().toString();
		String email = request.email();

		if (userViewRepository.findByEmail(email).isPresent()) {
			throw new ApplicationConflict("USER_ALREADY_EXISTS", String.format("Email: %s", email));
		}

		ProviderUserCreated keycloakUser = createIdentityProviderUser(request, role);

		UserAggregate aggregate = loadUser(userId);
		User user = getUser(userId, request, keycloakUser.identityProviderId());
		aggregate.subscribe(user);

		return new UserSubscribedResponse(user.getId(), user.getEmail().getValue(), user.getUserIdentityProviderId(),  user.getRole(), user.getStatus());

	}

	@Transactional
	public UserResponse unsubscribe(UnsubscribeRequest request) {

		UserView loggedUser = getLoggedUser();
		String loggedUserId = loggedUser.id();
		
		disableIdentityProviderUser(loggedUser.userIdentityProviderId());
		
		UserAggregate aggregate = loadUser(loggedUserId);
		aggregate.unsubscribe(request.reason());
		return new UserResponse(loggedUserId, aggregate.email(), loggedUser.userIdentityProviderId(), aggregate.role(), UserStatus.DISABLE);
	}

	@Transactional
	public UserResponse suspend(SuspendRequest request) {

		UserAggregate aggregate = loadUser(request.userId());

		UserView loggedUser = getLoggedUser();

		aggregate.suspend(request.reason(), loggedUser.id());
		return new UserResponse(request.userId(), aggregate.email(), loggedUser.userIdentityProviderId(), aggregate.role(), UserStatus.SUSPENDED);
	}

	@Transactional
	public UserResponse unsuspend(SuspendRequest request) {
		UserAggregate aggregate = loadUser(request.userId());

		UserView loggedUser = getLoggedUser();

		aggregate.unsuspend(request.reason(), loggedUser.id());
		return new UserResponse(request.userId(), aggregate.email(), loggedUser.userIdentityProviderId(), aggregate.role(), UserStatus.ACTIVE);
	}

	private UserView getLoggedUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null) {
			throw new InvalidUser("User not logged");
		}

		Jwt jwt = (Jwt) auth.getPrincipal();
		String email = jwt.getClaim("email");

		if (email == null) {
			throw new UserNotFound("Email not present in token");
		}

		return userViewRepository.findByEmail(email).orElseThrow(() -> new UserNotFound(String.format("Email: %s", email)));

	}

	private User getUser(String userId, SubscribeRequest request, String identityProviderId) {
		return User.create(userId, request.email(), request.name(), request.lastname(), identityProviderId, UserRole.READER);
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

	private ProviderUserCreated createIdentityProviderUser(SubscribeRequest request, String role) {
		
		try {	
			return identityProviderService.createUser(request.email(), request.password(), request.name(), request.lastname(), role);
		}catch (KeycloakException ex) {
		    throw mapIdentityProviderException(ex);
		}
	}

	private void disableIdentityProviderUser(String userIdentityProviderId) {
		try {	
			identityProviderService.disableUser(userIdentityProviderId);
		}catch (KeycloakException ex) {
		    throw mapIdentityProviderException(ex);
		}
	}
	
	private RuntimeException mapIdentityProviderException(KeycloakException ex) {
		
		String message = ex.getMessage();
		
		if (ex.getHttpStatusCode().equals(HttpStatus.CONFLICT)) {
		    return new ApplicationConflict("USER_ALREADY_EXISTS", message);
		}

		if (ex.getHttpStatusCode().equals(HttpStatus.BAD_REQUEST)) {
		    return new InvalidUserData(message);
		}
		
		if (ex.getHttpStatusCode().equals(HttpStatus.FORBIDDEN)) {
		    return new AuthorizationDeniedException(message);
		}

		return new UserCreationError(message);
	}

	public UserResponse getUserProfile(String userId) {
		
		UserView loggedUser = getLoggedUser();
		
		if (loggedUser.role() == UserRole.READER && !loggedUser.id().equalsIgnoreCase(userId)) {
			throw new UserNotFound("Reader user ID %s cannot see user ID %s profile".formatted(loggedUser.id(), userId));
		}
		
		Optional<UserView> user = userViewRepository.findById(userId);
		
		if(user.isEmpty()) {
			throw new UserNotFound("User ID %s".formatted(userId));
		}
	
		UserView userView = user.get();
		
		return new UserResponse(userView.id(), userView.email(), userView.userIdentityProviderId(), userView.role(), userView.status());
	
	}

	public UsersResponse getUsers() {
		List<UserView> users = userViewRepository.findAll();
		
		if(users.isEmpty()) {
			return new UsersResponse(List.of());
		}
		
		return new UsersResponse(users.stream().map(u -> new UserResponse(u.id(), u.email(), u.userIdentityProviderId(), u.role(), u.status())).toList());
	}
	
}
