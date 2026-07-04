package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.domain.events.LoanEventType;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.sharedcorelibrary.eventstore.EventCategory;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.sharedcorelibrary.replay.AbstractReplayService;
import tools.jackson.databind.JsonNode;

@Service
public class LoanReplayService extends AbstractReplayService<LoanEventEntity> {

	private final LoanEventRepository loanEventRepository;
	private final LoanViewReplayRepository loanViewReplayRepository;
	private final UserViewReplayRepository userViewReplayRepository;
	private final LoanProjectionReplay loanProjection;
	private final UserProjectionReplay userProjection;

	Map<String, Consumer<LoanEventEntity>> handlers = Map.ofEntries(
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, LoanEventType.LoanRequested), this::handleLoanRequested),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, LoanEventType.LoanConfirmed), this::handleLoanConfirmed),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, LoanEventType.LoanCanceled), this::handleLoanCanceled),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, LoanEventType.LoanReturned), this::handleLoanReturned),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, LoanEventType.LoanReserved), this::handleLoanReserved),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, LoanEventType.LoanFailed), this::handleLoanFailed),
			Map.entry("%s:%s".formatted(EventCategory.CONSUMER, IntegrationEventTypes.BOOK_RESERVED), this::handleEventReactor),
			Map.entry("%s:%s".formatted(EventCategory.CONSUMER, IntegrationEventTypes.BOOK_RESERVATION_REJECTED), this::handleEventReactor),
			Map.entry("%s:%s".formatted(EventCategory.CONSUMER, IntegrationEventTypes.BOOK_BORROWED), this::handleEventReactor),
			Map.entry("%s:%s".formatted(EventCategory.CONSUMER, IntegrationEventTypes.BOOK_BORROW_REJECTED), this::handleEventReactor),
			Map.entry("%s:%s".formatted(EventCategory.CONSUMER, IntegrationEventTypes.USER_SUBSCRIBED), this::handleUserSubscribed),
			Map.entry("%s:%s".formatted(EventCategory.CONSUMER, IntegrationEventTypes.USER_UNSUBSCRIBED), this::handleUserUnsubscribed),
			Map.entry("%s:%s".formatted(EventCategory.CONSUMER, IntegrationEventTypes.USER_SUSPENDED), this::handleUserSuspended),
			Map.entry("%s:%s".formatted(EventCategory.CONSUMER, IntegrationEventTypes.USER_UNSUSPENDED), this::handleUserUnsuspended));

	public LoanReplayService(LoanEventRepository loanEventRepository, LoanViewReplayRepository loanViewReplayRepository, 
			UserViewReplayRepository userViewReplayRepository, LoanProjectionReplay loanProjection, UserProjectionReplay userProjection) {
		this.loanEventRepository = loanEventRepository;
		this.loanViewReplayRepository = loanViewReplayRepository;
		this.userViewReplayRepository = userViewReplayRepository;
		this.loanProjection = loanProjection;
		this.userProjection = userProjection;
	}

	@Override
	protected void createTempTable() {
		loanViewReplayRepository.createTempTable();
		userViewReplayRepository.createTempTable();
	}

	@Override
	protected List<LoanEventEntity> loadEvents() {
		return loanEventRepository.findAllEvents();
	}

	@Override
	protected void swapTables() {
		loanViewReplayRepository.swapTables();
		userViewReplayRepository.swapTables();
	}

	@Override
	protected void dropTempTable() {
		loanViewReplayRepository.dropTempTable();
		userViewReplayRepository.dropTempTable();
	}

	@Override
	protected void apply(LoanEventEntity event) {
		applyToTempTable(event);
	}

	private void applyToTempTable(LoanEventEntity event) {
		String key = buildKey(event);

		Consumer<LoanEventEntity> handler = handlers.get(key);

		if (handler == null) {
			logger.warn("Replay not defined for {}", key);
			return;
		}

		handler.accept(event);

	}

	private String buildKey(LoanEventEntity event) {
		return "%s:%s".formatted(event.getEventCategory(), event.getEventType());
	}

	private void handleLoanRequested(LoanEventEntity entity) {
		loanProjection.loanInsert(getLoanRequestedPayload(entity.getPayload()), entity.getOccurredAt());
	}

	private void handleLoanConfirmed(LoanEventEntity entity) {
		loanProjection.confirmLoan(getLoanPayload(entity.getPayload()), entity.getOccurredAt());
	}

	private void handleLoanCanceled(LoanEventEntity entity) {
		loanProjection.cancelLoan(getLoanPayload(entity.getPayload()), entity.getOccurredAt());
	}

	private void handleLoanReturned(LoanEventEntity entity) {
		loanProjection.returnLoan(getLoanPayload(entity.getPayload()), entity.getOccurredAt());
	}

	private void handleLoanReserved(LoanEventEntity entity) {
		loanProjection.reserveLoan(getLoanPayload(entity.getPayload()), entity.getOccurredAt());
	}

	private void handleLoanFailed(LoanEventEntity entity) {
		loanProjection.failLoan(getLoanPayload(entity.getPayload()), entity.getOccurredAt());
	}

	private LoanRequestedIntegrationPayload getLoanRequestedPayload(JsonNode payload) {

		String loanId = getId(payload);
		String isbn = getIsbn(payload);
		String userId = getUserId(payload);
		
		JsonNode period = payload.get("period");
		
		if(period == null) {
			throw new IllegalStateException("Missing field period");
		}
		
		LocalDate startDate = LocalDate.parse(getStartDate(period));
		LocalDate endDate = LocalDate.parse(getEndDate(period));

		return new LoanRequestedIntegrationPayload(loanId, isbn, userId, startDate, endDate);
	}
	
	private LoanIntegrationPayload getLoanPayload(JsonNode payload) {
		
		String id = getId(payload);
		String isbn = getIsbn(payload);
		String userId = getUserId(payload);
		
		return new LoanIntegrationPayload(id, isbn, userId);
	}

	private void handleEventReactor(LoanEventEntity entity) {
		logger.info("Replay not needed for reactor event, {}", entity.getEventType());
	}
	
	private void handleUserSubscribed(LoanEventEntity entity) {
		userProjection.handleSubscribeUser(getUserSubscribedPayload(entity.getPayload()), entity.getOccurredAt());
	}

	private void handleUserUnsubscribed(LoanEventEntity entity) {
		userProjection.handleUpdateUserStatus(getUserIntegrationPayload(entity.getPayload(), UserStatus.DISABLE), entity.getOccurredAt());
	}

	private void handleUserSuspended(LoanEventEntity entity) {
		userProjection.handleUpdateUserStatus(getUserIntegrationPayload(entity.getPayload(), UserStatus.SUSPENDED), entity.getOccurredAt());
	}

	private void handleUserUnsuspended(LoanEventEntity entity) {
		userProjection.handleUpdateUserStatus(getUserIntegrationPayload(entity.getPayload(), UserStatus.ACTIVE), entity.getOccurredAt());
	}
	
	private UserSubscribedIntegrationPayload getUserSubscribedPayload(JsonNode payload) {

		String userId = getUserId(payload);
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
	
	private String getRequired(JsonNode jsonPayload, String field) {

		if (!jsonPayload.has(field) || jsonPayload.get(field).isNull()) {
			throw new IllegalStateException("Missing field %s".formatted(field));
		}

		return jsonPayload.get(field).asString();
	}

	private String getId(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "id");
	}
	
	private String getIsbn(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "isbn");
	}
	
	private String getStartDate(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "start");
	}

	private String getEndDate(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "end");
	}

	private String getEmail(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "email");
	}
	
	private String getName(JsonNode jsonPayload) {
		return jsonPayload.get("name").asString();
	}
	
	private String getLastname(JsonNode jsonPayload) {
		return jsonPayload.get("lastname").asString();
	}
	
	private String getUserIdentityProviderId(JsonNode jsonPayload) {
		return jsonPayload.get("userIdentityProviderId").asString();
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
			return null;
		}

		try {
			return UserRole.valueOf(role);
		} catch (Exception e) {
			return null;
		}
		
	}

	private String getUserId(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "userId");
	}

}
