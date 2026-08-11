package mentoring.acomi.userservice.application.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import mentoring.acomi.userservice.application.aggregates.UserAggregateFactory;
import mentoring.acomi.userservice.application.dto.SubscribeRequest;
import mentoring.acomi.userservice.application.dto.SuspendRequest;
import mentoring.acomi.userservice.application.dto.UnsubscribeRequest;
import mentoring.acomi.userservice.application.dto.UserDetail;
import mentoring.acomi.userservice.application.dto.UserRegisterRequest;
import mentoring.acomi.userservice.application.dto.UserRegisteredDto;
import mentoring.acomi.userservice.application.dto.UserResponse;
import mentoring.acomi.userservice.application.dto.UserSubscribedResponse;
import mentoring.acomi.userservice.application.dto.UsersResponse;
import mentoring.acomi.userservice.application.errors.IdentityProviderException;
import mentoring.acomi.userservice.application.errors.InvalidUser;
import mentoring.acomi.userservice.application.errors.InvalidUserData;
import mentoring.acomi.userservice.application.errors.UserCreationError;
import mentoring.acomi.userservice.application.errors.UserNotFound;
import mentoring.acomi.userservice.application.generator.CardNumberGenerator;
import mentoring.acomi.userservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.userservice.application.sso.IdentityProviderService;
import mentoring.acomi.userservice.application.sso.ProviderUserCreated;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.domain.errors.ApplicationConflict;
import mentoring.acomi.userservice.domain.model.CardNumber;
import mentoring.acomi.userservice.domain.model.User;

@Service
public class UserService {

	private final UserViewQueryRepository userViewRepository;
	private final IdentityProviderService identityProviderService;
	private final CardNumberGenerator cardNumberGenerator;
	private final UserAggregateFactory aggregateFactory;
	
	public UserService(UserViewQueryRepository userViewRepository, IdentityProviderService identityProviderService, 
		 CardNumberGenerator cardNumberGenerator, UserAggregateFactory aggregateFactory) {
		this.userViewRepository = userViewRepository;
		this.identityProviderService = identityProviderService;
		this.cardNumberGenerator = cardNumberGenerator;
		this.aggregateFactory = aggregateFactory;
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
		
		UserAggregate aggregate = aggregateFactory.create(loggedUserId);
		aggregate.unsubscribe(request.reason());
		return new UserResponse(loggedUserId, aggregate.email(), loggedUser.name(), loggedUser.lastname(), loggedUser.cardNumber(),
				aggregate.role(), UserStatus.DISABLED);
	}

	@Transactional
	public UserResponse suspend(SuspendRequest request) {

		UserAggregate aggregate = aggregateFactory.create(request.userId());

		UserView loggedUser = getLoggedUser();

		aggregate.suspend(request.reason(), loggedUser.id());
		return new UserResponse(request.userId(), aggregate.email(), loggedUser.name(), loggedUser.lastname(), 
				loggedUser.cardNumber(), aggregate.role(), UserStatus.SUSPENDED);
	}

	@Transactional
	public UserResponse unsuspend(SuspendRequest request) {
		UserAggregate aggregate = aggregateFactory.create(request.userId());

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

		UserAggregate aggregate = aggregateFactory.create(userId);
		
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

	private ProviderUserCreated createIdentityProviderUser(UserRegisteredDto user) {
		
		try {
			return identityProviderService.createUser(user.email(), user.password(), user.name(), user.lastname(), user.role());
		}catch (IdentityProviderException ex) {
		    throw mapIdentityProviderException(ex);
		}
	}

	private void deleteIdentityProviderUser(String userIdentityProviderId) {
		try {	
			identityProviderService.deleteUser(userIdentityProviderId);
		}catch (IdentityProviderException ex) {
		    throw mapIdentityProviderException(ex);
		}
	}
	
	private RuntimeException mapIdentityProviderException(IdentityProviderException ex) {
		
		return switch (ex.error()) {
		
			case USER_ALREADY_EXISTS -> {
				yield new ApplicationConflict("USER_ALREADY_EXISTS", ex.getMessage());
			}
			
			case INVALID_USER_DATA -> {
				yield new InvalidUserData(ex.getMessage());
			}
			case AUTHORIZATION_DENIED -> {
				yield new AuthorizationDeniedException(ex.getMessage());
			}
			
			default -> {
				yield new UserCreationError(ex.getMessage());
			}
		};

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
