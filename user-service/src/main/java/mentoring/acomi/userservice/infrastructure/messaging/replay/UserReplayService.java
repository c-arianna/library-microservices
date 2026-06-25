package mentoring.acomi.userservice.infrastructure.messaging.replay;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import mentoring.acomi.sharedlibrary.eventstore.EventCategory;
import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;
import mentoring.acomi.sharedlibrary.replay.AbstractReplayService;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.domain.events.UserEventType;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserEventEntity;
import tools.jackson.databind.JsonNode;

@Service
public class UserReplayService extends AbstractReplayService<UserEventEntity>{

	private final UserEventRepository userEventRepository;
	private final UserViewReplayRepository userViewReplayRepository;
	private final UserProjectionReplay projection;

	private final Logger logger = LogManager.getLogger(UserReplayService.class);

	Map<String, Consumer<UserEventEntity>> handlers = Map.ofEntries(
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, UserEventType.UserSubscribed), this::handleUserSubscribed),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, UserEventType.UserUnsubscribed), this::handleUserUnsubscribed),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, UserEventType.UserSuspended), this::handleUserSuspended),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, UserEventType.UserUnsuspended), this::handleUserUnsuspended));

	public UserReplayService(UserEventRepository userEventRepository, UserViewReplayRepository userViewReplayRepository, UserProjectionReplay projection) {
		this.userEventRepository = userEventRepository;
		this.userViewReplayRepository = userViewReplayRepository;
		this.projection = projection;
	}

	@Override
	protected void createTempTable() {
		userViewReplayRepository.createTempTable();		
	}

	@Override
	protected List<UserEventEntity> loadEvents() {
		return userEventRepository.findAllEvents();
	}

	@Override
	protected void apply(UserEventEntity event) {
		applyToTempTable(event);		
	}

	@Override
	protected void swapTables() {
		userViewReplayRepository.swapTables();		
	}

	@Override
	protected void dropTempTable() {
		userViewReplayRepository.dropTempTable();		
	}
	
	private void applyToTempTable(UserEventEntity entity) {

		String key = buildKey(entity);

		Consumer<UserEventEntity> handler = handlers.get(key);

		if (handler == null) {
			logger.warn("Replay not defined for {}", key);
			return;
		}

		handler.accept(entity);
	}

	private void handleUserSubscribed(UserEventEntity entity) {
		projection.subscribeUser(getUserSubscribedPayload(entity.getPayload()), entity.getOccurredAt());
	}

	private void handleUserUnsubscribed(UserEventEntity entity) {
		projection.unsubscribeUser(getUserIntegrationPayload(entity.getPayload(), UserStatus.DISABLE), entity.getOccurredAt());
	}

	private void handleUserSuspended(UserEventEntity entity) {
		projection.suspendUser(getUserIntegrationPayload(entity.getPayload(), UserStatus.SUSPENDED), entity.getOccurredAt());
	}

	private void handleUserUnsuspended(UserEventEntity entity) {
		projection.unsuspendUser(getUserIntegrationPayload(entity.getPayload(), UserStatus.ACTIVE), entity.getOccurredAt());
	}

	private UserSubscribedIntegrationPayload getUserSubscribedPayload(JsonNode payload) {

		String userId = getId(payload);
		String email = getEmail(payload);
		String name = getName(payload);
		String lastname = getLastname(payload);
		String userIdentityProviderId = getUserIdentityProviderId(payload);

		UserStatus status = getUserStatus(payload);

		UserRole role = getUserRole(payload);

		return new UserSubscribedIntegrationPayload(userId, email, name, lastname, userIdentityProviderId, status, role);
	}

	private UserIntegrationPayload getUserIntegrationPayload(JsonNode payload, UserStatus status) {
		String userId = getUserId(payload);
		return new UserIntegrationPayload(userId, status);
	}
	
	private UserStatus getUserStatus(JsonNode jsonPayload) {
		
		String status = jsonPayload.get("status").asString();

		if (status == null) {
			throw new IllegalStateException("Missing field status");
		}

		try {
			return UserStatus.valueOf(status);
		} catch (Exception e) {
			throw new IllegalStateException("Invalid field status %s".formatted(status));
		}
		
	}
	
    private UserRole getUserRole(JsonNode jsonPayload) {
		
		String role = jsonPayload.get("role").asString();

		if (role == null) {
			throw new IllegalStateException("Missing field role");
		}

		try {
			return UserRole.valueOf(role);
		} catch (Exception e) {
			throw new IllegalStateException("Invalid field role %s".formatted(role));
		}
		
	}

    private String getId(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "id");
	}
    
	private String getUserId(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "userId");
	}
	
	private String getEmail(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "email");
	}
	
	private String getName(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "name");
	}
	
	private String getLastname(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "lastname");
	}
	
	private String getUserIdentityProviderId(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "userIdentityProviderId");
	}
	
	private String getRequired(JsonNode jsonPayload, String field) {

		if (!jsonPayload.has(field) || jsonPayload.get(field).isNull()) {
			throw new IllegalStateException("Missing field %s".formatted(field));
		}

		return jsonPayload.get(field).asString();
	}
	
	private String buildKey(UserEventEntity event) {
		return "%s:%s".formatted(event.getEventCategory(), event.getEventType());
	}

}
