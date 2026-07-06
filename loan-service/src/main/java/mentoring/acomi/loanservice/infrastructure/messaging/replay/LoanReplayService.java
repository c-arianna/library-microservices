package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import mentoring.acomi.loanservice.application.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.domain.events.LoanEventType;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanFailedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.sharedcorelibrary.eventstore.EventCategory;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.replay.AbstractReplayService;

@Service
public class LoanReplayService extends AbstractReplayService<LoanEventEntity> {

	private final LoanEventRepository loanEventRepository;
	private final LoanViewReplayRepository loanViewReplayRepository;
	private final UserViewReplayRepository userViewReplayRepository;
	private final LoanProjectionReplay loanProjection;
	private final UserProjectionReplay userProjection;
    private final EventPayloadMapper payloadMapper;
    private final LoanReplayEventMapper replayMapper;
    
    private final Map<IntegrationEventTypes, Consumer<IntegrationEventEnvelope<?>>> handlers;
    
	public LoanReplayService(LoanEventRepository loanEventRepository, LoanViewReplayRepository loanViewReplayRepository,
			UserViewReplayRepository userViewReplayRepository, LoanProjectionReplay loanProjection,
			UserProjectionReplay userProjection, EventPayloadMapper payloadMapper, LoanReplayEventMapper replayMapper) {
		this.loanEventRepository = loanEventRepository;
		this.loanViewReplayRepository = loanViewReplayRepository;
		this.userViewReplayRepository = userViewReplayRepository;
		this.loanProjection = loanProjection;
		this.userProjection = userProjection;
		this.payloadMapper = payloadMapper;
		this.replayMapper = replayMapper;
		
		handlers = Map.ofEntries(
				Map.entry(IntegrationEventTypes.LOAN_REQUESTED, this::handleLoanRequested),
				Map.entry(IntegrationEventTypes.LOAN_CONFIRMED, this::handleLoanConfirmed),
				Map.entry(IntegrationEventTypes.LOAN_CANCELED, this::handleLoanCanceled),
				Map.entry(IntegrationEventTypes.LOAN_RETURNED, this::handleLoanReturned),
				Map.entry(IntegrationEventTypes.LOAN_RESERVED, this::handleLoanReserved),
				Map.entry(IntegrationEventTypes.LOAN_FAILED, this::handleLoanFailed),
				Map.entry(IntegrationEventTypes.BOOK_RESERVED, this::handleEventReactor),
				Map.entry(IntegrationEventTypes.BOOK_RESERVATION_REJECTED, this::handleEventReactor),
				Map.entry(IntegrationEventTypes.BOOK_BORROWED, this::handleEventReactor),
				Map.entry(IntegrationEventTypes.BOOK_BORROW_REJECTED, this::handleEventReactor),
				Map.entry(IntegrationEventTypes.USER_SUBSCRIBED, this::handleUserSubscribed),
				Map.entry(IntegrationEventTypes.USER_UNSUBSCRIBED, this::handleUserUnsubscribed),
				Map.entry(IntegrationEventTypes.USER_SUSPENDED, this::handleUserSuspended),
				Map.entry(IntegrationEventTypes.USER_UNSUSPENDED, this::handleUserUnsuspended));
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

		if (event.getSchemaVersion() != 1) {
		    throw new IllegalStateException("Unsupported schema version %s".formatted(event.getSchemaVersion()));
		}
		
		if (event.getEventCategory().equalsIgnoreCase(EventCategory.CONSUMER.name())) {
			IntegrationEventTypes eventType = IntegrationEventTypes.valueOf(event.getEventType());
			replayEvent(event, eventType);
		} else {
			replayProducerEvent(event);
		}

	}

	private void replayProducerEvent(LoanEventEntity event) {
		LoanEventType loanEventType = LoanEventType.valueOf(event.getEventType());
		IntegrationEventEnvelope<?> eventEnvelope  = getIntegrationEnvelopeEvent(event, loanEventType);
		replayEvent(eventEnvelope);
	}

	private void replayEvent(LoanEventEntity event, IntegrationEventTypes eventType) {
		IntegrationEventEnvelope<?> eventEnvelope = new IntegrationEventEnvelope<>(event.getEventId(), eventType, "",
				event.getAggregateId(), event.getAggregateType(), event.getEventVersion(), event.getOccurredAt(),
				event.getSchemaVersion(), event.getPayload());
		replayEvent(eventEnvelope);
	}

	private void replayEvent(IntegrationEventEnvelope<?> eventEnvelope) {

		IntegrationEventTypes eventType = eventEnvelope.eventType();
		String eventId = eventEnvelope.eventId();

		if (eventType == IntegrationEventTypes.LOAN_CONFIRM_REQUESTED) {
			logger.info("No replay needed for process event {}", eventEnvelope.eventType());
			return;
		}

		Consumer<IntegrationEventEnvelope<?>> consumer = handlers.get(eventType);

		if (consumer == null) {
			logger.warn("No handler found for event {} ({})", eventId, eventType);
			return;
		}

		consumer.accept(eventEnvelope);

	}
	
	private IntegrationEventEnvelope<?> getIntegrationEnvelopeEvent(LoanEventEntity event, LoanEventType loanEventType) {
		Object payload = replayMapper.toIntegrationPayload(loanEventType, event.getPayload());
		IntegrationEventTypes eventType = replayMapper.toIntegrationEventType(loanEventType);
		return new IntegrationEventEnvelope<>(event.getEventId(), eventType, "",
				event.getAggregateId(), event.getAggregateType(), event.getEventVersion(), event.getOccurredAt(),
				event.getSchemaVersion(), payload);
	}
	
	private void handleLoanRequested(IntegrationEventEnvelope<?> event) {
		LoanRequestedIntegrationPayload payload = payloadMapper.mapAndValidate(event.payload(), LoanRequestedIntegrationPayload.class);
		loanProjection.loanInsert(payload, event.occurredAt());
	}
	
	private void handleLoanConfirmed(IntegrationEventEnvelope<?> event) {
		LoanIntegrationPayload payload = loadLoanPayload(event);
		loanProjection.confirmLoan(payload.loanId(), event.occurredAt());
	}

	private void handleLoanCanceled(IntegrationEventEnvelope<?> event) {
		LoanIntegrationPayload payload = loadLoanPayload(event);
		loanProjection.cancelLoan(payload.loanId(), event.occurredAt());
	}

	private void handleLoanReturned(IntegrationEventEnvelope<?> event) {
		LoanIntegrationPayload payload = loadLoanPayload(event);
		loanProjection.returnLoan(payload.loanId(), event.occurredAt());
	}

	private void handleLoanReserved(IntegrationEventEnvelope<?> event) {
		LoanIntegrationPayload payload = loadLoanPayload(event);
		loanProjection.reserveLoan(payload.loanId(), event.occurredAt());
	}

	private void handleLoanFailed(IntegrationEventEnvelope<?> event) {
		LoanFailedIntegrationPayload payload = payloadMapper.mapAndValidate(event.payload(), LoanFailedIntegrationPayload.class);
		loanProjection.failLoan(payload.loanId(), event.occurredAt());
	}
	
	private void handleEventReactor(IntegrationEventEnvelope<?> event) {
		logger.info("Replay not needed for reactor event, {}", event.eventType());
	}
	
	private void handleUserSubscribed(IntegrationEventEnvelope<?> event) {
		UserSubscribedIntegrationPayload payload = payloadMapper.mapAndValidate(event.payload(), UserSubscribedIntegrationPayload.class);
		userProjection.handleSubscribeUser(payload, event.occurredAt());
	}

	private void handleUserUnsubscribed(IntegrationEventEnvelope<?> event) {
		UserIntegrationPayload payload = loadUserPayload(event);
		userProjection.handleUpdateUserStatus(payload, event.occurredAt());
	}

	private void handleUserSuspended(IntegrationEventEnvelope<?> event) {
		UserIntegrationPayload payload = loadUserPayload(event);
		userProjection.handleUpdateUserStatus(payload, event.occurredAt());
	}

	private void handleUserUnsuspended(IntegrationEventEnvelope<?> event) {
		UserIntegrationPayload payload = loadUserPayload(event);
		userProjection.handleUpdateUserStatus(payload, event.occurredAt());
	}

	private UserIntegrationPayload loadUserPayload(IntegrationEventEnvelope<?> event) {
		return payloadMapper.mapAndValidate(event.payload(), UserIntegrationPayload.class);
	}
	
	private LoanIntegrationPayload loadLoanPayload(IntegrationEventEnvelope<?> event) {
		return payloadMapper.mapAndValidate(event.payload(), LoanIntegrationPayload.class);
	}
}
