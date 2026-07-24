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

import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.UserFilter;
import mentoring.acomi.userservice.application.aggregates.UserAggregate;
import mentoring.acomi.userservice.application.errors.InvalidUser;
import mentoring.acomi.userservice.application.errors.InvalidUserData;
import mentoring.acomi.userservice.application.errors.UserCreationError;
import mentoring.acomi.userservice.application.errors.UserNotFound;
import mentoring.acomi.userservice.application.generator.CardNumberGenerator;
import mentoring.acomi.userservice.application.messaging.EventDispatcher;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.userservice.application.sso.IdentityProviderService;
import mentoring.acomi.userservice.application.sso.ProviderUserCreated;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.domain.errors.ApplicationConflict;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.UserEventType;
import mentoring.acomi.userservice.domain.model.CardNumber;
import mentoring.acomi.userservice.domain.model.User;
import mentoring.acomi.userservice.infrastructure.dto.SubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.SuspendRequest;
import mentoring.acomi.userservice.infrastructure.dto.UnsubscribeRequest;
import mentoring.acomi.userservice.infrastructure.dto.UserDetail;
import mentoring.acomi.userservice.infrastructure.dto.UserRegisterRequest;
import mentoring.acomi.userservice.infrastructure.dto.UserRegisteredDto;
import mentoring.acomi.userservice.infrastructure.dto.UserResponse;
import mentoring.acomi.userservice.infrastructure.dto.UserSubscribedResponse;
import mentoring.acomi.userservice.infrastructure.dto.UsersResponse;
import mentoring.acomi.userservice.infrastructure.messaging.UserIntegrationPublisherEventVersions;
import mentoring.acomi.userservice.infrastructure.sso.keycloak.errors.KeycloakException;

@Service
public class UserService {

	private final UserViewQueryRepository userViewRepository;
	private final UserEventRepository userEventRepository;
	private final EventDispatcher eventDispatcher;
	private final IdentityProviderService identityProviderService;
	private final CardNumberGenerator cardNumberGenerator;
	
	private final Logger logger = LogManager.getLogger(UserService.class);

	public UserService(UserViewQueryRepository userViewRepository, UserEventRepository userEventRepository, 
		 EventDispatcher eventDispatcher, IdentityProviderService identityProviderService, CardNumberGenerator cardNumberGenerator) {
		this.userViewRepository = userViewRepository;
		this.userEventRepository = userEventRepository;
		this.eventDispatcher = eventDispatcher;
		this.identityProviderService = identityProviderService;
		this.cardNumberGenerator = cardNumberGenerator;
	}

	@Transactional
	public UserSubscribedResponse subscribe(SubscribeRequest request) {

		 UserRegisteredDto userRegistered = new UserRegisteredDto(request.name(), request.lastname(), request.email(), 
				 request.password(), UserRole.READER);
		 
		return createUser(userRegistered);
	}

	@Transactional
	public UserResponse unsubscribe(UnsubscribeRequest request) {

		UserView loggedUser = getLoggedUser();
		String loggedUserId = loggedUser.id();
		
		deleteIdentityProviderUser(loggedUser.userIdentityProviderId());
		
		UserAggregate aggregate = loadUser(loggedUserId);
		aggregate.unsubscribe(request.reason());
		return new UserResponse(loggedUserId, aggregate.email(), loggedUser.name(), loggedUser.lastname(), loggedUser.cardNumber(),
				aggregate.role(), UserStatus.DISABLED);
	}

	@Transactional
	public UserResponse suspend(SuspendRequest request) {

		UserAggregate aggregate = loadUser(request.userId());

		UserView loggedUser = getLoggedUser();

		aggregate.suspend(request.reason(), loggedUser.id());
		return new UserResponse(request.userId(), aggregate.email(), loggedUser.name(), loggedUser.lastname(), 
				loggedUser.cardNumber(), aggregate.role(), UserStatus.SUSPENDED);
	}

	@Transactional
	public UserResponse unsuspend(SuspendRequest request) {
		UserAggregate aggregate = loadUser(request.userId());

		UserView loggedUser = getLoggedUser();

		aggregate.unsuspend(request.reason(), loggedUser.id());
		return new UserResponse(request.userId(), aggregate.email(), loggedUser.name(), loggedUser.lastname(), loggedUser.cardNumber(),
				aggregate.role(), UserStatus.ACTIVE);
	}
	
	@Transactional
	public UserSubscribedResponse register(UserRegisterRequest request) {
		 UserRegisteredDto userRegistered = new UserRegisteredDto(request.name(), request.lastname(), request.email(), request.password(),
		    		request.role());
		 
		return createUser(userRegistered);
	}

	private UserSubscribedResponse createUser(UserRegisteredDto userRegistered) {
		
		String userId = UUID.randomUUID().toString();
		String email = userRegistered.email();

		if (userViewRepository.findNotDisabledUserByEmail(email).isPresent()) {
			throw new ApplicationConflict("USER_ALREADY_EXISTS", String.format("Email: %s", email));
		}
		
		ProviderUserCreated keycloakUser = createIdentityProviderUser(userRegistered);

		UserAggregate aggregate = loadUser(userId);
		
		String cardNumber = generateCardNumber(userRegistered.role());
		
		User user = getUser(userId, userRegistered, keycloakUser.identityProviderId(), cardNumber);
		
		aggregate.subscribe(user);

		return new UserSubscribedResponse(user.getId(), user.getEmail().getValue(), user.getUserIdentityProviderId(),
				user.getCardNumber().value(), user.getRole(), user.getStatus());
	}

	private String generateCardNumber(UserRole role) {
		
		if(UserRole.ADMIN.equals(role) || UserRole.LIBRARIAN.equals(role)) {
			return null;
		}
		
		CardNumber cardNumber =  cardNumberGenerator.generate();
		
		return cardNumber.value();
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

		return userViewRepository.findNotDisabledUserByEmail(email).orElseThrow(() -> new UserNotFound(String.format("Email: %s", email)));

	}

	private User getUser(String userId, UserRegisteredDto user, String identityProviderId, String cardNumber) {
		return User.create(userId, user.email(), user.name(), user.lastname(), identityProviderId, cardNumber, user.role());
	}

	private UserAggregate loadUser(String userId) {
		List<UserEvent> events = userEventRepository.loadStream(userId);
		Consumer<UserEvent> dispatch = event -> {
			userEventRepository.appendToStream(event, getSchemaVersion(event.type()));
			try {
				eventDispatcher.dispatch(event);
			} catch (Exception e) {
				logger.error("[Dispatch] error after event persistence, eventType={}", event.type(), e);
			}
		};

		return new UserAggregate(userId, dispatch, events);
	}

	private int getSchemaVersion(UserEventType eventType) {
		
		return switch(eventType) {
			case UserSubscribed -> {
				yield UserIntegrationPublisherEventVersions.USER_SUBSCRIBED;
			}
			case UserUnsubscribed ->{
				yield UserIntegrationPublisherEventVersions.USER_UNSUBSCRIBED;
			}
			case UserSuspended ->{
				yield UserIntegrationPublisherEventVersions.USER_SUSPENDED;
			}
			case UserUnsuspended ->{
				yield UserIntegrationPublisherEventVersions.USER_UNSUSPENDED;
			}
			case LibraryCardAssigned -> {
				yield UserIntegrationPublisherEventVersions.LIBRARY_CARD_ASSIGNED;
			}
		};
	}

	private ProviderUserCreated createIdentityProviderUser(UserRegisteredDto user) {
		
		try {
			return identityProviderService.createUser(user.email(), user.password(), user.name(), user.lastname(), user.role());
		}catch (KeycloakException ex) {
		    throw mapIdentityProviderException(ex);
		}
	}

	private void deleteIdentityProviderUser(String userIdentityProviderId) {
		try {	
			identityProviderService.deleteUser(userIdentityProviderId);
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

	public UserDetail getUserDetail(String userId) {
		
		Optional<UserView> user = userViewRepository.findById(userId);
		
		if(user.isEmpty()) {
			throw new UserNotFound("User ID %s".formatted(userId));
		}
	
		UserView userView = user.get();
		
		return new UserDetail(userView.id(), userView.email(), userView.name(), userView.lastname(), userView.cardNumber(), 
				userView.userIdentityProviderId(),  userView.status(), userView.role());
	
	}
	
	public UserDetail getUserProfile() {
		UserView loggedUser = getLoggedUser();
		return new UserDetail(loggedUser.id(), loggedUser.email(), loggedUser.name(), loggedUser.lastname(), loggedUser.cardNumber(), 
				loggedUser.userIdentityProviderId(), loggedUser.status(), loggedUser.role());
	}

	public UsersResponse getUsers(UserFilter filter) {
		List<UserView> users = userViewRepository.find(filter);
		
		if(users.isEmpty()) {
			return new UsersResponse(List.of());
		}
		
		return new UsersResponse(users.stream().map(u -> new UserResponse(u.id(), u.email(), u.name(), u.lastname(), u.cardNumber(),
				u.role(), u.status())).toList());
	}
	
}
