package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.domain.events.BookEventType;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.sharedlibrary.eventstore.EventCategory;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.replay.AbstractReplayService;
import tools.jackson.databind.JsonNode;

@Service
public class BookReplayService extends AbstractReplayService<BookEventEntity>{

	private final BookEventRepository bookEventRepository;
	private final BookViewReplayRepository bookViewReplayRepository;
	private final BookProjectionReplay projection;

	Map<String, Consumer<BookEventEntity>> handlers = Map.ofEntries(
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, BookEventType.BookRegistered), this::handleBookRegistered),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, BookEventType.BookCopiesAdded), this::handleUpdateCopies),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, BookEventType.BookCopiesRemoved), this::handleUpdateCopies),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, BookEventType.BookBorrowed), this::handleBookBorrowed),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, BookEventType.BookReleased), this::handleBookReleased),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, BookEventType.BookReserved), this::handleBookReserved),
			Map.entry("%s:%s".formatted(EventCategory.PRODUCER, BookEventType.BookReturned), this::handleBookReturned),
			Map.entry("%s:%s".formatted(EventCategory.CONSUMER, IntegrationEventTypes.LOAN_REQUESTED), this::handleEventReactor),
			Map.entry("%s:%s".formatted(EventCategory.CONSUMER, IntegrationEventTypes.LOAN_CONFIRM_REQUESTED), this::handleEventReactor),
			Map.entry("%s:%s".formatted(EventCategory.CONSUMER, IntegrationEventTypes.LOAN_CANCELED), this::handleEventReactor),
			Map.entry("%s:%s".formatted(EventCategory.CONSUMER, IntegrationEventTypes.LOAN_RETURNED), this::handleEventReactor));

	public BookReplayService(BookViewReplayRepository bookViewReplayRepository, BookEventRepository bookEventRepository, BookProjectionReplay projection) {
		this.bookEventRepository = bookEventRepository;
		this.bookViewReplayRepository = bookViewReplayRepository;
		this.projection = projection;
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

		String key = buildKey(entity);

		Consumer<BookEventEntity> handler = handlers.get(key);

		if (handler == null) {
			logger.warn("Replay not defined for {}", key);
			return;
		}

		handler.accept(entity);

	}

	private String buildKey(BookEventEntity event) {
		return "%s:%s".formatted(event.getEventCategory(), event.getEventType());
	}

	private void handleEventReactor(BookEventEntity entity) {
		logger.info("Replay not needed for reactor event, {}", entity.getEventType());
	}

	private void handleBookReturned(BookEventEntity entity) {
		projection.returnBorrowed(getBookLoanPayload(entity.getPayload()), entity.getOccurredAt());
	}

	private void handleBookReleased(BookEventEntity entity) {
		projection.release(getBookLoanPayload(entity.getPayload()), entity.getOccurredAt());
	}

	private void handleBookBorrowed(BookEventEntity entity) {
		projection.borrow(getBookLoanPayload(entity.getPayload()), entity.getOccurredAt());
	}

	private void handleBookReserved(BookEventEntity entity) {
		projection.reserve(getBookLoanPayload(entity.getPayload()), entity.getOccurredAt());
	}

	private void handleUpdateCopies(BookEventEntity entity) {
		projection.updateCopies(getBookCopiesUpdatedPayload(entity.getPayload(), entity.getEventType()), entity.getOccurredAt());
	}

	private void handleBookRegistered(BookEventEntity entity) {
		projection.addBook(getBookRegisteredPayload(entity.getPayload()), entity.getOccurredAt());
	}

	private BookRegisteredIntegrationPayload getBookRegisteredPayload(JsonNode jsonPayload) {
			
		String isbn = getIsbn(jsonPayload);
		String author = getAuthor(jsonPayload);
		String title = getTitle(jsonPayload);
		String description = getDescription(jsonPayload);
		
		return new BookRegisteredIntegrationPayload(isbn, author, title, description);
	}
	
	private BookLoanIntegrationPayload getBookLoanPayload(JsonNode jsonPayload) {
				
		String isbn = getIsbn(jsonPayload);
		String loanId = getLoanId(jsonPayload);
		String userId = getUserId(jsonPayload);
		
		return new BookLoanIntegrationPayload(isbn, loanId, userId);
	}
	
	private BookCopiesUpdatedIntegrationPayload getBookCopiesUpdatedPayload(JsonNode jsonPayload, String eventType) {
		
		String isbn = getIsbn(jsonPayload);
		int quantity = getQuantity(jsonPayload);
		
		if(eventType.equalsIgnoreCase(BookEventType.BookCopiesRemoved.name())){
			quantity*= -1;
		}
		
		return new BookCopiesUpdatedIntegrationPayload(isbn, quantity);
	}

	private String getDescription(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "description");
	}

	private String getTitle(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "title");
	}

	private String getAuthor(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "author");
	}

	private String getIsbn(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "isbn");
	}
	
	private String getLoanId(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "loanId");
	}
	
	private String getUserId(JsonNode jsonPayload) {
		return getRequired(jsonPayload, "userId");
	}

	private int getQuantity(JsonNode jsonPayload) {
		String field = "quantity";
		if (!jsonPayload.has(field) || jsonPayload.get(field).isNull()) {
			throw new IllegalStateException("Missing field %s".formatted(field));
		}
		return jsonPayload.get(field).asInt();
	}

	private String getRequired(JsonNode jsonPayload, String field) {
		
		if (!jsonPayload.has(field) || jsonPayload.get(field).isNull()) {
			throw new IllegalStateException("Missing field %s".formatted(field));
		}
		
		return jsonPayload.get(field).asString();
	}

}
