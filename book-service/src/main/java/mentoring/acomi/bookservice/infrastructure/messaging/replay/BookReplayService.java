package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.domain.events.BookEventType;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.sharedcorelibrary.eventstore.EventCategory;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.replay.AbstractReplayService;

@Service
public class BookReplayService extends AbstractReplayService<BookEventEntity> {

	private final BookEventRepository bookEventRepository;
	private final BookViewReplayRepository bookViewReplayRepository;
	private final BookProjectionReplay projection;
	private final BookReplayEventMapper replayMapper;
	private final EventPayloadMapper payloadMapper;

	private final Map<IntegrationEventTypes, Consumer<IntegrationEventEnvelope<?>>> handlers;

	public BookReplayService(BookViewReplayRepository bookViewReplayRepository, BookEventRepository bookEventRepository,
			BookProjectionReplay projection, BookReplayEventMapper replayMapper, EventPayloadMapper payloadMapper) {
		this.bookEventRepository = bookEventRepository;
		this.bookViewReplayRepository = bookViewReplayRepository;
		this.projection = projection;
		this.replayMapper = replayMapper;
		this.payloadMapper = payloadMapper;

		handlers = Map.ofEntries(Map.entry(IntegrationEventTypes.BOOK_REGISTERED, this::handleBookRegistered),
				Map.entry(IntegrationEventTypes.BOOK_COPIES_UPDATED, this::handleUpdateCopies),
				Map.entry(IntegrationEventTypes.BOOK_BORROWED, this::handleBookBorrowed),
				Map.entry(IntegrationEventTypes.BOOK_RELEASED, this::handleBookReleased),
				Map.entry(IntegrationEventTypes.BOOK_RESERVED, this::handleBookReserved),
				Map.entry(IntegrationEventTypes.BOOK_RETURNED, this::handleBookReturned),
				Map.entry(IntegrationEventTypes.LOAN_REQUESTED, this::handleEventReactor),
				Map.entry(IntegrationEventTypes.LOAN_CONFIRM_REQUESTED, this::handleEventReactor),
				Map.entry(IntegrationEventTypes.LOAN_CANCELED, this::handleEventReactor),
				Map.entry(IntegrationEventTypes.LOAN_RETURNED, this::handleEventReactor));
	}

	@Override
	protected void createTempTable() {
		bookViewReplayRepository.createTempTable();
	}

	@Override
	protected List<BookEventEntity> loadEvents() {
		return bookEventRepository.findAllEvents();
	}

	@Override
	protected void apply(BookEventEntity event) {
		applyToTempTable(event);

	}

	@Override
	protected void swapTables() {
		bookViewReplayRepository.swapTables();
	}

	@Override
	protected void dropTempTable() {
		bookViewReplayRepository.dropTempTable();
	}

	private void applyToTempTable(BookEventEntity entity) {

		if (entity.getSchemaVersion() != 1) {
		    throw new IllegalStateException("Unsupported schema version %s".formatted(entity.getSchemaVersion()));
		}
		
		if (entity.getEventCategory().equalsIgnoreCase(EventCategory.CONSUMER.name())) {
			IntegrationEventTypes eventType = IntegrationEventTypes.valueOf(entity.getEventType());
			replayEvent(entity, eventType);
		} else {
			replayProducerEvent(entity);
		}

	}
	
	private void replayEvent(BookEventEntity event, IntegrationEventTypes eventType) {
		IntegrationEventEnvelope<?> eventEnvelope = new IntegrationEventEnvelope<>(event.getEventId(), eventType, "",
				event.getAggregateId(), event.getAggregateType(), event.getEventVersion(), event.getOccurredAt(),
				event.getSchemaVersion(), event.getPayload());
		replayEvent(eventEnvelope);
	}
	
	private void replayEvent(IntegrationEventEnvelope<?> eventEnvelope) {

		IntegrationEventTypes eventType = eventEnvelope.eventType();
		String eventId = eventEnvelope.eventId();

		if (eventType == IntegrationEventTypes.BOOK_RESERVATION_REJECTED || eventType == IntegrationEventTypes.BOOK_BORROW_REJECTED) {
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
	
	private void replayProducerEvent(BookEventEntity event) {
		BookEventType bookEventType = BookEventType.valueOf(event.getEventType());
		IntegrationEventEnvelope<?> eventEnvelope  = getIntegrationEnvelopeEvent(event, bookEventType);
		replayEvent(eventEnvelope);
	}
	
	private IntegrationEventEnvelope<?> getIntegrationEnvelopeEvent(BookEventEntity event, BookEventType loanEventType) {
		Object payload = replayMapper.toIntegrationPayload(loanEventType, event.getPayload());
		IntegrationEventTypes eventType = replayMapper.toIntegrationEventType(loanEventType);
		return new IntegrationEventEnvelope<>(event.getEventId(), eventType, "",
				event.getAggregateId(), event.getAggregateType(), event.getEventVersion(), event.getOccurredAt(),
				event.getSchemaVersion(), payload);
	}
	
	private void handleEventReactor(IntegrationEventEnvelope<?> event) {
		logger.info("Replay not needed for reactor event, {}", event.eventType());
	}

	private void handleBookReturned(IntegrationEventEnvelope<?> event) {
		BookLoanIntegrationPayload payload = loadBookLoanPayload(event);
		projection.returnBorrowed(payload, event.occurredAt());
	}

	private void handleBookReleased(IntegrationEventEnvelope<?> event) {
		BookLoanIntegrationPayload payload = loadBookLoanPayload(event);
		projection.release(payload, event.occurredAt());
	}

	private void handleBookBorrowed(IntegrationEventEnvelope<?> event) {
		BookLoanIntegrationPayload payload = loadBookLoanPayload(event);
		projection.borrow(payload, event.occurredAt());
	}

	private void handleBookReserved(IntegrationEventEnvelope<?> event) {
		BookLoanIntegrationPayload payload = loadBookLoanPayload(event);
		projection.reserve(payload, event.occurredAt());
	}

	private void handleUpdateCopies(IntegrationEventEnvelope<?> event) {
		BookCopiesUpdatedIntegrationPayload payload = payloadMapper.mapAndValidate(event.payload(), BookCopiesUpdatedIntegrationPayload.class);
		projection.updateCopies(payload, event.occurredAt());
	}

	private void handleBookRegistered(IntegrationEventEnvelope<?> event) {
		BookRegisteredIntegrationPayload payload = payloadMapper.mapAndValidate(event.payload(), BookRegisteredIntegrationPayload.class);
		projection.addBook(payload, event.occurredAt());
	}

	private BookLoanIntegrationPayload loadBookLoanPayload(IntegrationEventEnvelope<?> event) {
		return payloadMapper.mapAndValidate(event.payload(), BookLoanIntegrationPayload.class);
	}

}